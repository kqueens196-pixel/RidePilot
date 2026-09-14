# Trending Consecutive & Cut Panel Matrix Engine

TRENDING_CUT_PANELS = {
    1: {"trend": "678", "cut": "123"},
    2: {"trend": "345", "cut": "789"},
    6: {"trend": "123", "cut": "678"},
    7: {"trend": "234", "cut": "789"}
}

def execute_trending_cut_matrix():
    otc = [1, 2, 6, 7]

    print("\n" + "="*65)
    print("      SRIDEVI NIGHT: TRENDING CP & CONSECUTIVE ENGINE")
    print("="*65)
    print("Audit Base              : 379 & 567 Cut-Family Expansion")
    print(f"LOCKED 4 OTC            : {otc}")
    print("-" * 65)

    print("TRENDING PANELS & CUT PAIRS:")
    for ank in otc:
        tp = TRENDING_CUT_PANELS[ank]["trend"]
        cp = TRENDING_CUT_PANELS[ank]["cut"]
        print(f"  Ank [{ank}] -> Trending: {tp}  |  Family Cut Panel: {cp}")

    print("-" * 65)
    print("LOCKED JODIS            : 26    71    (Backup: 12, 67)")
    print("-" * 65)
    print("PRE-MARKET HALF SANGAM  :")
    print("  345 x 6     234 x 1     2 x 123     7 x 678")
    print("-" * 65)
    print("PRE-MARKET FULL SANGAM (TOP 4):")
    print("  1. 345 x 123   (Jodi 26)")
    print("  2. 234 x 678   (Jodi 71)")
    print("  3. 678 x 345   (Jodi 12)")
    print("  4. 123 x 234   (Jodi 67)")
    print("="*65 + "\n")

if __name__ == "__main__":
    execute_trending_cut_matrix()
