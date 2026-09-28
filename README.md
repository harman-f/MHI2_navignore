# MHI2_navignore

Research and historical Java patches for keeping **native MHI2 navigation/guidance active while CarPlay or Android Auto navigation is active**.

> [!WARNING]
> Experimental / firmware-specific. Keep a recovery path and do not assume compatibility from the train prefix alone.

## Patch families

The repository preserves two main combined CP/AA patch families:

- `thevaan/CP_AA_VW` — Volkswagen / Škoda / SEAT style Java stack
- `thevaan/CP_AA_Audi` — Audi / Porsche / Bentley style Java stack

These patches work by overriding selected LSD/HMI Java classes and suppressing stock navigation-ownership side effects that normally stop or displace native guidance when projected navigation becomes active.

## Important 2026 finding: loader order matters

For some firmware families, copying the JAR into `/eso/hmi/lsd/jars/` is not sufficient.

A public AU57x test report shows the existing Audi-family patch working for **CarPlay and Android Auto** when the train's `lsd.sh` / `j9` launch is adjusted so that the custom NavIgnore JAR is loaded **before** the main JXE/stock classes.

Therefore compatibility must include the actual Java loader/classpath arrangement, not only class names or train-family matching.

Do **not** blindly replace `lsd.sh` with one from another train. Audit and minimally adapt the target firmware's own startup script.

## 2026 research note

See **[RESEARCH_2026.md](RESEARCH_2026.md)** for:

- what the historical patches actually change;
- VW-family vs Audi-family behavior;
- AU57x / `lsd.sh` / `j9` class-loading findings;
- compatibility-matrix guidance;
- interpretation of the 2026 `custom.sh` deployment report;
- issue triage;
- relationship to the newer CarPlay AltScreen / Virtual Cockpit project.

## Related public research

- NavIgnore discussion / field reports:  
  https://github.com/Mr-MIBonk/M.I.B._More-Incredible-Bash/discussions/93
- CarPlay Auxiliary Screen / Virtual Cockpit research:  
  https://github.com/harman-f/mhi2_altscreen_carplay

## Scope

This repository targets **MHI2**. MOI3 and other generations must not be assumed compatible.

The sources are retained as research material and as a basis for building a future guarded target-audit / installer rather than a universal blind-copy package.
