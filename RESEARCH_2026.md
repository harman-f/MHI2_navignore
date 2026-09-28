# MHI2_navignore — 2026 research and compatibility notes

Status: research / experimental  
Updated: 2026-09-28

This note documents what the historical Java patches in this repository actually do, what newer public test reports teach us about compatibility, and how NavIgnore relates to newer MHI2 auxiliary-display research.

## 1. What NavIgnore actually changes

NavIgnore is **not** a navigation activation/FEC patch and it does not create a CarPlay or Android Auto video stream.

The historical patches override selected Java classes in the MHI2 HMI/LSD process so that the stock logic does not enforce the usual mutual exclusion between native navigation and projected navigation.

The source currently preserved in this repository shows two related patch families:

- **VW / Škoda / SEAT**: `thevaan/CP_AA_VW`
- **Audi / Porsche / Bentley**: `thevaan/CP_AA_Audi`

### VW / Škoda / SEAT

The modified `CarPlayModeHandling.java` keeps the normal CarPlay navigation state updates, but suppresses stock side effects that would otherwise:

- stop native route guidance when CarPlay becomes the active navigation owner;
- notify the external/cluster guidance coordination layer that smartphone guidance became active/inactive;
- force selected mode changes back toward the stock single-navigation-owner model.

The modified Android Auto `RequestHandler.java` follows the same principle: projected-navigation state is still reported, while the stock native-guidance shutdown / Exbox-guidance coordination calls are suppressed.

### Audi / Porsche / Bentley

The modified `AndroidAuto2NavHandler.java` changes navigation-focus handling so that projected navigation can become active without the normal native-navigation ownership transition being applied in the same way.

The practical model is therefore:

```text
stock:
projected navigation active
        -> ownership/focus coordination
        -> native navigation/guidance is stopped or displaced

NavIgnore:
projected navigation active
        -> keep the required projection state/focus notifications
        -> suppress selected stock mutual-exclusion side effects
        -> native navigation can remain active
```

This also explains why a working patch must **shadow the stock Java classes**. Merely copying a JAR onto the filesystem is not enough if it is loaded after the stock classes.

## 2. Classpath / loader order is part of compatibility

The old installation scripts mostly copy `NavActiveIgnore.jar` into:

```text
/net/mmx/mnt/app/eso/hmi/lsd/jars/
```

For several trains that was sufficient because of the way the LSD Java runtime assembled its class path.

A public 2026 field report for **AU57x** changes the old assumption that the patch fails because AU57x has a fundamentally incompatible Java structure. The report states that `thevaan/CP_AA_Audi` works for both CarPlay and Android Auto when the AU57x `lsd.sh` / `j9` launch is adjusted so that the custom JAR is loaded **before** the main JXE/classes.

Source:

- Mr-MIBonk/M.I.B._More-Incredible-Bash discussion #93  
  https://github.com/Mr-MIBonk/M.I.B._More-Incredible-Bash/discussions/93

The important compatibility rule is therefore:

> **Do not classify a train as compatible/incompatible only from class names or from the presence of the JAR directory. Inspect the target train's actual LSD/J9 startup and class-loading order.**

For AU57x in particular, the current evidence points to a **loader/integration difference**, not automatically to a different patch algorithm.

### Do not copy a foreign lsd.sh blindly

Use the target firmware's own `lsd.sh` as the baseline and make the minimum required classpath/JAR-order change. A script from another train can contain different VM arguments, image/JXE names, memory settings or target-specific startup options.

Before any change:

1. preserve the original `lsd.sh`;
2. record the exact firmware train and MU version;
3. compare the original `j9` invocation and class/JXE order;
4. ensure the NavIgnore JAR has precedence over the stock class source;
5. keep a tested SSH/recovery path.

## 3. The 2026 custom.sh report

A September 2026 reply in the same public discussion shows using M.I.B. `custom.sh` to launch the repository's `command.sh`.

That is useful as an **execution/deployment path**, but it should not be confused with the AU57x compatibility fix itself:

```text
custom.sh
   -> runs command.sh
   -> command.sh installs/copies files

lsd.sh / j9 class order
   -> decides whether the replacement Java classes actually shadow stock
```

If a train needs an LSD/J9 ordering change, simply invoking `command.sh` through `custom.sh` does not remove that requirement.

## 4. Compatibility should be treated as a matrix

Historical train-family selection in `thevaan/command.sh` is intentionally simple:

- `VWG1*`, `SKG1*`, `SEG1*` -> VW-family JAR
- other supported MHI2 families -> Audi-family JAR

That is useful as a first classification, but it is not a sufficient compatibility gate.

A safer matrix records at least:

| Property | Why it matters |
| --- | --- |
| Exact train | Java/HMI generation and packaging |
| MU version | Concrete class implementation |
| Brand/family | Selects the patch family |
| LSD/J9 launch form | Determines shadow/preload behavior |
| Stock class presence/signature | Confirms that the override matches the target |
| CarPlay / Android Auto | The two paths patch different classes |
| Native guidance retained | Functional NavIgnore result |
| Projection guidance retained | Confirms projection was not broken |
| Voice / steering-wheel behavior | Detects collateral HMI ownership effects |
| Reboot / HMI start | Detects class-linkage or loader failures |

### Currently unresolved example

The public discussion contains an open 2026 question for **MHI2_ER_SKG13_P4514 / MU1163**. Do not infer compatibility with P452x/MU1440 solely from the shared SKG13 family name. Compare the relevant Java classes and LSD launch first.

## 5. Relation to CarPlay AltScreen / Virtual Cockpit work

NavIgnore and CarPlay auxiliary-display support solve different layers of the problem.

NavIgnore operates at the **HMI Java navigation ownership/focus policy** layer.

The newer public project:

https://github.com/harman-f/mhi2_altscreen_carplay

researches the **CarPlay Auxiliary/ScreenAlt -> MHI2 -> Virtual Cockpit** path, including the secondary-screen control and video transport.

Current public AltScreen research indicates that normal navigation-app/ownership changes can occur on an existing auxiliary stream rather than requiring a complete stream recreation. That does **not** replace NavIgnore: one mechanism controls HMI navigation ownership policy, while the other transports/presents the projected cluster display.

For combined experiments, treat these as separate state machines:

```text
HMI / Java ownership policy       -> NavIgnore
CarPlay auxiliary lifecycle       -> ScreenAlt / Type-111 control
video transport to the cluster    -> MHI2 display/MOST path
cluster presentation/layout       -> VC/AID display policy
```

This separation is useful when debugging: a working video stream does not prove that the native/projected navigation ownership policy is correct, and a working NavIgnore JAR does not prove that auxiliary-screen transport is present.

## 6. Repository issue triage (2026-09-28)

At the time of this update the repository has no open pull requests.

The remaining open issues should be interpreted as follows:

- **#1 — installation help**: documentation gap; this note addresses the architecture but a guarded installer is still preferable to blind manual copying.
- **#3 — MOI3 navigation problem**: MOI3 is outside this repository's MHI2 Java-patch scope and should not be treated as a NavIgnore compatibility report.
- **#4 — CarLife support**: not implemented by the current CP/AA patch sources; requires separate class/DSI analysis.

## 7. Recommended next engineering step

Before producing a new universal installer, build a small **target audit / dry-run** that does not modify the unit and reports:

- train and MU;
- hash/size of the target `lsd.sh`;
- exact `j9` launch line;
- JXE/JAR/classpath order;
- presence of the classes overridden by the selected patch family;
- selected NavIgnore family;
- whether classpath precedence would actually load the replacement first.

Only after that audit passes should an installer modify `lsd.sh` or copy a replacement JAR.

This is safer than extending the old train-name wildcard logic.

## Public references

- Historical source and patches: https://github.com/harman-f/MHI2_navignore
- Public NavIgnore test discussion: https://github.com/Mr-MIBonk/M.I.B._More-Incredible-Bash/discussions/93
- Public AltScreen / Virtual Cockpit research: https://github.com/harman-f/mhi2_altscreen_carplay
