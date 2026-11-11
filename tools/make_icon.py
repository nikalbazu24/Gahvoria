# Gahvoria: genera el cartucho (grafito y coral) como iconos del lanzador, capa adaptativa, monocromo, marca y banner.
# Uso: python3 tools/make_icon.py
from PIL import Image, ImageDraw, ImageFilter
import sys
import os
RES=os.path.join(os.path.dirname(os.path.abspath(__file__)),"..","app","src","main","res")+"/"
S=1024
GR0=(13,14,18); GR1=(28,29,38)          # grafito
A1=(255,122,107); A2=(255,179,138); A3=(169,180,255)  # coral
INK=(14,12,20,255); WHITE=(255,255,255,255)
def lerp(a,b,t): return tuple(int(a[i]+(b[i]-a[i])*t) for i in range(3))
def grad(t): return lerp(A1,A2,t*2) if t<.5 else lerp(A2,A3,(t-.5)*2)
def bg(size):
    img=Image.new("RGB",(size,size)); px=img.load()
    for y in range(size):
        for x in range(size):
            t=min(1,max(0,(x+y)/(2*size)))
            px[x,y]=lerp(GR0,GR1,t)
    return img.convert("RGBA")
def cart(size, mono=False, transparent=True):
    k=size/2048.0
    def P(*pts): return [(x*k,y*k) for x,y in pts]
    x0,x1,y0,y1=1024-440, 1024+440, 260, 1790
    # coordinates in S space, then scale
    ch=200
    body=[(x0+ch,y0),(x1-ch,y0),(x1,y0+ch),(x1,y1-80),(x1-80,y1),(x0+80,y1),(x0,y1-80),(x0,y0+ch)]
    layer=Image.new("RGBA",(size,size),(0,0,0,0)); d=ImageDraw.Draw(layer)
    # cuerpo con degradado coral
    grd=Image.new("RGBA",(size,size),(0,0,0,0)); gd=ImageDraw.Draw(grd)
    for y in range(int(y0*k),int(y1*k)+1):
        t=min(1,max(0,(y-y0*k)/((y1-y0)*k)))
        gd.line([(0,y),(size,y)],fill=(WHITE if mono else grad(t)+(255,)))
    mask=Image.new("L",(size,size),0); ImageDraw.Draw(mask).polygon(P(*body),fill=255)
    layer.paste(grd,(0,0),mask)
    # ventana oscura
    lw=[x0+120,y0+170,x1-120,y0+960]
    ImageDraw.Draw(layer).rounded_rectangle([lw[0]*k,lw[1]*k,lw[2]*k,lw[3]*k],radius=60*k,fill=(WHITE if mono else INK))
    cxm=(lw[0]+lw[2])/2; cyy=(lw[1]+lw[3])/2-40
    rh=[(cxm,cyy-250),(cxm+190,cyy),(cxm,cyy+250),(cxm-190,cyy)]
    ImageDraw.Draw(layer).polygon(P(*rh),fill=(INK if mono else A1+(255,)))
    # ranuras
    for i in range(5):
        xx=x0+170+i*(x1-x0-340)/4
        ImageDraw.Draw(layer).rounded_rectangle([(xx-22)*k,(y1-300)*k,(xx+22)*k,(y1-70)*k],radius=22*k,fill=(WHITE if mono else INK))
    return layer
def compose(size, bgimg=True):
    base=bg(size) if bgimg else Image.new("RGBA",(size,size),(0,0,0,0))
    if bgimg:
        glow=cart(size); g=glow.filter(ImageFilter.GaussianBlur(size*0.05))
        a=g.split()[3].point(lambda v:int(v*0.4)); g.putalpha(a); base.alpha_composite(g)
    base.alpha_composite(cart(size))
    return base
# Lanzador completo (legado), cuadrado, a color
legacy={"mipmap-mdpi":48,"mipmap-hdpi":72,"mipmap-xhdpi":96,"mipmap-xxhdpi":144,"mipmap-xxxhdpi":192}
full=compose(1024)
for folder,px in legacy.items():
    full.resize((px,px),Image.LANCZOS).convert("RGB").save(RES+folder+"/ic_launcher.png")
# Capa adaptativa: 432px, zona segura ~ 66%
def fg(size, mono=False):
    inner=int(size*0.66)
    c=cart(inner, mono=mono)
    out=Image.new("RGBA",(size,size),(0,0,0,0)); out.alpha_composite(c,((size-inner)//2,(size-inner)//2)); return out
fg(432).save(RES+"drawable-nodpi/ic_launcher_foreground.png")
fg(432,mono=True).save(RES+"drawable-nodpi/ic_launcher_monochrome.png")
# Banner TV (320x180)
b=Image.new("RGBA",(320,180),(0,0,0,0)); bg2=bg(180); bg2=bg2.resize((320,180))
c=cart(150); b=bg2.copy(); b.alpha_composite(c,(85,15)); b.convert("RGB").save(RES+"mipmap-xhdpi/ic_banner.png")
print("ok")
