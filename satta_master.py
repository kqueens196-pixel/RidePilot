# Sridevi Night: Live Open 0 (Panel 226) Close Matrix

CLOSE_PANELS = {
    1: ["146", "245", "678"],
    6: ["150", "240", "367"],
    4: ["149", "239", "347"],
    9: ["180", "270", "379"]
}

def execute_open_zero():
    op = "226"
    oa = 0

    # Strong Close balanced with Open 0
    close_otc = [1, 4, 6, 9]

    print("\n" + "="*65)
    print("      SRIDEVI NIGHT: LIVE CLOSE ENGINE (OPEN 0 LOCKED)")
    print("="*65)
    print(f"CONFIRMED LIVE OPEN     : Panel {op} | Open Ank {oa}")
    print("FAILURE AUDIT LEARNED   : 5 Cut 0 Triggered | Double 2 Pattern (226)")
    print("-" * 65)
    print(f"STRONG CLOSE OTC        : {close_otc}  (Core: 1, 6 | Support: 4, 9)")
    print("-" * 65)

    print("TOP CLOSE PANELS        :")
    for ank in close_otc:
        panels = "  ".join(CLOSE_PANELS[ank])
        print(f"  Ank [{ank}] -> {panels}")

    print("-" * 65)
    print("RUNNING JODIS (OPEN 0)  : 01    06    04    09")
    print("-" * 65)
    print("LIVE RUNNING HALF SANGAM:")
    print("  226 x 1     226 x 6     226 x 4     226 x 9")
    print("-" * 65)
    print("TOP RUNNING FULL SANGAM :")
    print("  1. 226 x 146   (Jodi 01)")
    print("  2. 226 x 240   (Jodi 06)")
    print("  3. 226 x 149   (Jodi 04)")
    print("  4. 226 x 180   (Jodi 09)")
    print("="*65 + "\n")

if __name__ == "__main__":
    execute_open_zero()
