import argparse, json, random
from pathlib import Path
import torch
from torch import nn
from torch.utils.data import Dataset, DataLoader

PAD, BOS, EOS, UNK = 0, 1, 2, 3
ROOT = Path(__file__).resolve().parent
DATA = ROOT / "data" / "seed.jsonl"
OUT = ROOT / "artifacts" / "freemind_gru.pt"

def load_pairs():
    rows = [json.loads(x) for x in DATA.read_text(encoding="utf-8").splitlines() if x.strip()]
    return [(r["instruction"], r["response"]) for r in rows]

def build_vocab(pairs):
    chars = sorted(set("".join(a+b for a,b in pairs)))
    vocab = ["<pad>", "<bos>", "<eos>", "<unk>"] + chars
    return vocab, {c:i for i,c in enumerate(vocab)}

def encode(s, stoi, max_len):
    return [BOS] + [stoi.get(c, UNK) for c in s[:max_len-2]] + [EOS]

class Pairs(Dataset):
    def __init__(self, rows): self.rows = rows
    def __len__(self): return len(self.rows)
    def __getitem__(self, i): return self.rows[i]

def collate(batch):
    ms, mt = max(len(x[0]) for x in batch), max(len(x[1]) for x in batch)
    src = torch.full((len(batch), ms), PAD, dtype=torch.long)
    tgt = torch.full((len(batch), mt), PAD, dtype=torch.long)
    for i,(s,t) in enumerate(batch):
        src[i,:len(s)] = torch.tensor(s)
        tgt[i,:len(t)] = torch.tensor(t)
    return src,tgt

class GRUSeq2Seq(nn.Module):
    def __init__(self, vocab_size, emb=64, hidden=96):
        super().__init__()
        self.embedding = nn.Embedding(vocab_size, emb, padding_idx=PAD)
        self.encoder = nn.GRU(emb, hidden, batch_first=True)
        self.decoder = nn.GRU(emb, hidden, batch_first=True)
        self.projection = nn.Linear(hidden, vocab_size)
    def forward(self, src, tgt):
        _, state = self.encoder(self.embedding(src))
        decoded, _ = self.decoder(self.embedding(tgt[:,:-1]), state)
        return self.projection(decoded)

def make_data(pairs, stoi, max_len):
    rows = [(encode(q,stoi,max_len), encode(a,stoi,max_len)) for q,a in pairs]
    random.Random(17).shuffle(rows)
    n = len(rows)
    a,b = max(1,int(n*.8)), max(1,int(n*.9))
    return rows[:a], rows[a:b], rows[b:]

def loss_on(model, loader, loss_fn):
    model.eval(); total=count=0
    with torch.no_grad():
        for src,tgt in loader:
            logits=model(src,tgt); labels=tgt[:,1:]
            loss=loss_fn(logits.reshape(-1,logits.size(-1)),labels.reshape(-1))
            k=(labels!=PAD).sum().item(); total += loss.item()*k; count += k
    return total/max(count,1)

def train(epochs=30, batch_size=16):
    random.seed(17); torch.manual_seed(17)
    pairs=load_pairs(); vocab,stoi=build_vocab(pairs); max_len=180
    tr,va,te=make_data(pairs,stoi,max_len)
    train_loader=DataLoader(Pairs(tr),batch_size=batch_size,shuffle=True,collate_fn=collate)
    val_loader=DataLoader(Pairs(va),batch_size=batch_size,shuffle=False,collate_fn=collate)
    model=GRUSeq2Seq(len(vocab))
    optimizer=torch.optim.AdamW(model.parameters(),lr=0.002)
    loss_fn=nn.CrossEntropyLoss(ignore_index=PAD)
    for epoch in range(1,epochs+1):
        model.train(); total=count=0
        for src,tgt in train_loader:
            optimizer.zero_grad()
            logits=model(src,tgt); labels=tgt[:,1:]
            loss=loss_fn(logits.reshape(-1,logits.size(-1)),labels.reshape(-1))
            loss.backward(); nn.utils.clip_grad_norm_(model.parameters(),1.0); optimizer.step()
            k=(labels!=PAD).sum().item(); total += loss.item()*k; count += k
        val=loss_on(model,val_loader,loss_fn)
        print(f"epoch={epoch:03d} train_loss={total/max(count,1):.4f} val_loss={val:.4f}")
    OUT.parent.mkdir(parents=True,exist_ok=True)
    torch.save({"state_dict":model.state_dict(),"vocab":vocab,"config":{"emb":64,"hidden":96,"max_len":max_len}},OUT)
    print(f"Saved checkpoint: {OUT}")
    print(f"Dataset rows: {len(pairs)} | train: {len(tr)} | validation: {len(va)} | test: {len(te)}")
    print("Note: test split is reserved; low loss does not prove conversational quality.")

def chat():
    if not OUT.exists():
        raise SystemExit("Checkpoint missing. Run: python train.py --epochs 30")
    ckpt=torch.load(OUT,map_location="cpu",weights_only=False)
    vocab=ckpt["vocab"]; stoi={c:i for i,c in enumerate(vocab)}; cfg=ckpt["config"]
    model=GRUSeq2Seq(len(vocab),cfg["emb"],cfg["hidden"])
    model.load_state_dict(ckpt["state_dict"]); model.eval()
    print("FreeMind AI experimental chat. Type /exit to stop.")
    while True:
        q=input("You: ").strip()
        if q.lower()=="/exit": break
        src=torch.tensor([encode(q,stoi,cfg["max_len"])])
        out=[]
        with torch.no_grad():
            _,state=model.encoder(model.embedding(src))
            token=torch.tensor([[BOS]])
            for _ in range(140):
                decoded,state=model.decoder(model.embedding(token),state)
                nxt=int(model.projection(decoded[:,-1]).argmax(-1).item())
                if nxt==EOS: break
                if nxt in (PAD,BOS): break
                out.append(vocab[nxt]); token=torch.tensor([[nxt]])
        print("FreeMind:", "".join(out))

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--epochs",type=int,default=30)
    p.add_argument("--batch-size",type=int,default=16)
    p.add_argument("--chat",action="store_true")
    args=p.parse_args()
    chat() if args.chat else train(args.epochs,args.batch_size)
