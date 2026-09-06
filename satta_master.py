CUT = {0: 5, 1: 6, 2: 7, 3: 8, 4: 9, 5: 0, 6: 1, 7: 2, 8: 3, 9: 4}

PANELS = {
    1: ["128", "146", "245"],
    2: ["147", "237", "345"],
    6: ["150", "240"],
    7: ["124", "340"]
}

def next_day_engine():
    op = "199"
    jd = "92"
    cp = "390"
    oa = 9
    ca = 2

    total = (oa + ca) % 10   # 9+2 = 11 -> 1
    diff = abs(oa - ca)      # |9-2| = 7
    
    otc = sorted([total, CUT[total], diff, CUT[diff]]) # [1, 2, 6, 7]

    print("\n" + "="*55)
    print("       SRIDEVI NIGHT: NEXT DAY VERIFIED PREDICTION")
    print("="*55)
    print(f"Latest Confirmed Record : Panel {op} | Jodi {jd} | Panel {cp}")
    print(f"Formula Output          : Sum=1 (Cut 6) | Diff=7 (Cut 2)")
    print("-" * 55)
    print(f"NEXT DAY STRONG 4 OTC   : {otc}")
    print("-" * 55)
    print("TOP 10 FILTERED PANELS  :")
    idx = 1
    flat_panels = []
    for ank in otc:
        for p in PANELS[ank]:
            flat_panels.append(p)
            print(f"  {idx:02d}. Ank [{ank}] -> Panel: {p}")
            idx += 1

    print("-" * 55)
    print("STRONG JODIS            : 12   17   62   67   21   71   26   72")
    print("-" * 55)
    print("TOP 4 FULL SANGAM       :")
    print(f"  1. {flat_panels[0]} x {flat_panels[3]}")
    print(f"  2. {flat_panels[1]} x {flat_panels[4]}")
    print(f"  3. {flat_panels[6]} x {flat_panels[8]}")
    print(f"  4. {flat_panels[2]} x {flat_panels[9]}")
    print("="*55 + "\n")

if __name__ == "__main__":
    next_day_engine()
