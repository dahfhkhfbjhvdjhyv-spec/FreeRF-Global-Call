import argparse, json, os, random
import torch
from torch import nn

def main():
    p=argparse.ArgumentParser()
    p.add_argument("--steps",type=int,default=300)
    p.add_argument("--out",default="model-output")
    a=p.parse_args()
    random.seed(7); torch.manual_seed(7)
    text=open(os.path.join(os.path.dirname(__file__),"corpus.txt"),encoding="utf-8").read()
    chars=sorted(set(text)); stoi={c:i for i,c in enumerate(chars)}
    data=torch.tensor([stoi[c] for c in text],dtype=torch.long)
    vocab=len(chars); block=96; emb=64; heads=4; layers=2
    class TinyBanglaLM(nn.Module):
        def __init__(self):
            super().__init__()
            self.token=nn.Embedding(vocab,emb); self.pos=nn.Embedding(block,emb)
            layer=nn.TransformerEncoderLayer(emb,heads,dim_feedforward=128,batch_first=True,dropout=0.0)
            self.tr=nn.TransformerEncoder(layer,layers); self.norm=nn.LayerNorm(emb); self.head=nn.Linear(emb,vocab)
        def forward(self,x):
            t=x.size(1); pos=torch.arange(t,device=x.device)
            h=self.token(x)+self.pos(pos)[None,:,:]
            mask=torch.triu(torch.ones(t,t,device=x.device,dtype=torch.bool),diagonal=1)
            return self.head(self.norm(self.tr(h,mask=mask)))
    model=TinyBanglaLM(); opt=torch.optim.AdamW(model.parameters(),lr=3e-4)
    model.train()
    for step in range(a.steps):
        starts=torch.randint(0,len(data)-block-1,(16,))
        x=torch.stack([data[s:s+block] for s in starts])
        y=torch.stack([data[s+1:s+block+1] for s in starts])
        logits=model(x); loss=nn.functional.cross_entropy(logits.reshape(-1,vocab),y.reshape(-1))
        opt.zero_grad(); loss.backward(); torch.nn.utils.clip_grad_norm_(model.parameters(),1.0); opt.step()
        if (step+1)%50==0: print(f"step {step+1}/{a.steps} loss={loss.item():.4f}",flush=True)
    os.makedirs(a.out,exist_ok=True)
    torch.save({"state_dict":model.state_dict(),"stoi":stoi,"config":{"vocab":vocab,"block":block,"emb":emb,"heads":heads,"layers":layers}},os.path.join(a.out,"model.pt"))
    with open(os.path.join(a.out,"tokenizer.json"),"w",encoding="utf-8") as f: json.dump({"stoi":stoi},f,ensure_ascii=False)
    print("Saved randomly initialized then trained baseline checkpoint.")
if __name__=="__main__": main()
