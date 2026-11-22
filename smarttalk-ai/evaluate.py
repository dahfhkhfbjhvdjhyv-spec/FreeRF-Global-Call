import argparse, math, os
import torch
from torch import nn

def main():
    p=argparse.ArgumentParser()
    p.add_argument("--checkpoint",required=True)
    p.add_argument("--eval",default=os.path.join(os.path.dirname(__file__),"eval.txt"))
    a=p.parse_args()
    ck=torch.load(a.checkpoint,map_location="cpu",weights_only=False)
    stoi=ck["stoi"]; cfg=ck["config"]
    class TinyBanglaLM(nn.Module):
        def __init__(self):
            super().__init__()
            e,b,v=cfg["emb"],cfg["block"],cfg["vocab"]
            self.token=nn.Embedding(v,e); self.pos=nn.Embedding(b,e)
            layer=nn.TransformerEncoderLayer(e,cfg["heads"],dim_feedforward=128,batch_first=True,dropout=0.0)
            self.tr=nn.TransformerEncoder(layer,cfg["layers"])
            self.norm=nn.LayerNorm(e); self.head=nn.Linear(e,v)
        def forward(self,x):
            t=x.size(1)
            mask=torch.triu(torch.ones(t,t,dtype=torch.bool),diagonal=1)
            return self.head(self.norm(self.tr(self.token(x)+self.pos(torch.arange(t))[None,:,:],mask=mask)))
    model=TinyBanglaLM()
    model.load_state_dict(ck["state_dict"]); model.eval()
    text=open(a.eval,encoding="utf-8").read()
    known=sum(c in stoi for c in text); total=len(text)
    ids=torch.tensor([stoi.get(c,0) for c in text],dtype=torch.long)
    block=cfg["block"]; losses=[]
    with torch.no_grad():
        for start in range(0,max(0,len(ids)-1),block):
            x=ids[start:start+block]
            y=ids[start+1:start+block+1]
            if len(x)==0 or len(x)!=len(y): continue
            logits=model(x.unsqueeze(0))[0]
            losses.append(nn.functional.cross_entropy(logits,y).item())
    avg=sum(losses)/len(losses) if losses else float("inf")
    print(f"heldout_chars={total}")
    print(f"character_coverage={known/total:.4f}" if total else "character_coverage=0")
    print(f"mean_chunk_cross_entropy={avg:.4f}")
    print(f"perplexity={math.exp(min(avg,50)):.2f}")
    print("NOTE: perplexity measures next-character prediction, NOT answer accuracy or factual correctness.")
if __name__=="__main__": main()
