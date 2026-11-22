import argparse, torch
from torch import nn
p=argparse.ArgumentParser(); p.add_argument("--checkpoint",required=True); p.add_argument("--prompt",default="বাংলা"); p.add_argument("--tokens",type=int,default=120); a=p.parse_args()
ck=torch.load(a.checkpoint,map_location="cpu",weights_only=False); cfg=ck["config"]; stoi=ck["stoi"]; itos={v:k for k,v in stoi.items()}
class TinyBanglaLM(nn.Module):
 def __init__(self):
  super().__init__(); e=cfg["emb"]; b=cfg["block"]; v=cfg["vocab"]
  self.token=nn.Embedding(v,e); self.pos=nn.Embedding(b,e)
  l=nn.TransformerEncoderLayer(e,cfg["heads"],dim_feedforward=128,batch_first=True,dropout=0.0)
  self.tr=nn.TransformerEncoder(l,cfg["layers"]); self.norm=nn.LayerNorm(e); self.head=nn.Linear(e,v)
 def forward(self,x):
  t=x.size(1); mask=torch.triu(torch.ones(t,t,dtype=torch.bool),diagonal=1)
  return self.head(self.norm(self.tr(self.token(x)+self.pos(torch.arange(t))[None,:,:],mask=mask)))
m=TinyBanglaLM(); m.load_state_dict(ck["state_dict"]); m.eval()
ids=[stoi.get(c,0) for c in a.prompt] or [0]
for _ in range(a.tokens):
 x=torch.tensor([ids[-cfg["block"]:]])
 with torch.no_grad(): nxt=int(torch.multinomial(torch.softmax(m(x)[0,-1],dim=-1),1))
 ids.append(nxt)
print("".join(itos[i] for i in ids))
