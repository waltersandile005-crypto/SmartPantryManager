# Educational reconstruction of Smart Pantry Manager

This repository reconstructs a progression from a supplied, already completed
Android project. These commits were created during the reconstruction session,
using their actual creation timestamps. They are not evidence of the dates or
sequence of the original application's development. This reconstruction alone
does not establish compliance with an assignment requiring contemporaneous
development history.

No Git repository was present in the supplied folder, and the destination GitHub
account had no repositories when inspected. No existing history was replaced.

## Milestones

Version labels describe the following commits, in oldest-to-newest order. They
are documentation labels, not Android version changes or Git tags.

| Version | Commit subject after `reconstruct:` | What the stage introduces |
| --- | --- | --- |
| v0.1 | initialize Android project and pantry screen shell | Gradle wrapper/configuration, manifest, theme/resources, and launcher shell. |
| v0.2 | add pantry model and SQLite storage | PantryItem and pantry table with CRUD and cursor mapping. |
| v0.3 | display stored pantry items in RecyclerView | Read-only database-backed pantry list, row formatting, empty state, and refresh. |
| v0.4 | add validated ingredient creation | Add form, name/quantity checks, unit selection, optional expiry, insertion and feedback. |
| v0.5 | support pantry editing and deletion | Intent IDs, prefilled editing, updates, row deletion and list refresh. |
| v0.6 | add recipe models and seeded SQLite collection | Recipe models, linked tables, 18 complete recipe seeds and lookup methods. |
| v0.7 | add ingredient matching and suggested recipes | Existing name/unit rules, quantity matching, suggested recipe list and navigation. |
| v0.8 | add separate almost-there recipe suggestions | Separate results for exactly one unmet ingredient requirement. |
| v0.9 | add recipe details and recipe navigation | Details with quantities and method, row navigation and toolbar return. |
| v1.0 | add saved settings and complete project documentation | SharedPreferences settings, complete navigation, original README and this reconstruction record. |

Each milestone was built before committing. Intermediate stages are intended
for fresh installations; they do not introduce a cross-stage database migration
system. The supplied final database version and upgrade behavior are unchanged.

## Preservation

All 37 original source, resource, documentation and build-tooling files were
preserved byte-for-byte in the final state, verified with SHA-256 checksums against
an untouched pre-reconstruction snapshot. This document is the only additional
project file. The original README, including its author information, is unchanged.

Build outputs, `.gradle`, IDE caches, local SDK configuration and generated APKs
are excluded from version control. The Gradle wrapper JAR is intentionally tracked
as required build tooling. No application features or bug fixes were added.

## Existing behavior and limitations

- Every required recipe ingredient must be covered by one pantry row. Duplicate
  pantry rows are not summed.
- Names are trimmed/lowercased and simple plurals are normalized. Compatible mass
  and volume units are converted before quantity comparison.
- Incompatible/unknown units fall back to comparing raw numeric quantities. This
  is an existing limitation of the supplied strict-matching implementation.
- "Almost There" counts exactly one unmet requirement, including insufficient
  quantity, and remains separate from the strict suggestion list.
- Expiry dates are stored as text; they are not date-validated or used to exclude
  expired items from matching.
- Settings persist alert and unit-system choices but do not implement actual
  notifications or alter the application's units.
- SQLite persists across ordinary process restarts. The supplied `onUpgrade`
  drops and recreates tables; preservation across schema upgrades is not claimed.
- No automated unit or instrumentation tests were supplied in the project.

## Validation performed

- `assembleDebug` passed for each of the ten stages using the supplied Gradle
  wrapper and the locally installed Android SDK.
- The final `assembleDebug testDebugUnitTest` invocation succeeded. The unit-test
  task reported `NO-SOURCE`; it did not execute an existing test suite.
- The supplied original project also built successfully before reconstruction.
- An external Java harness ran 11 assertions against the unchanged
  `IngredientMatcher`: plural/case/whitespace normalization, an uncountable noun,
  mass and volume conversion boundaries, insufficient quantities, count units,
  and preservation of the existing incompatible-unit fallback. All passed.
- SHA-256 comparison verified all 37 supplied project files match the final
  reconstructed files exactly. No original application files were lost.
- Tracked-file checks excluded build/cache/IDE outputs. A scan for common GitHub,
  AWS and private-key credential patterns found no matches.

Build command (with `ANDROID_HOME` pointing to an installed SDK):

```sh
sh gradlew --console=plain assembleDebug testDebugUnitTest
```

The wrapper was invoked through `sh` because macOS blocked direct execution of
its downloaded copy. No wrapper file changes were needed.

### Emulator checks

External scripted UI checks ran on the `Medium_Phone` emulator (reported API 37)
against a debug build of the unchanged supplied application. They verified:

1. Fresh pantry empty state and exactly 18 seeded recipes.
2. Rejected empty ingredient name and zero quantity.
3. Ingredient creation and list display.
4. With only rice, no strict suggestions and Rice and Bean Bowl in Almost There.
5. With `rice: 0.15 kg` and `beans: 200 g`, Rice and Bean Bowl appears in strict
   suggestions, exercising both name normalization and mass conversion.
6. Recipe details display the ingredient quantities and method; return navigation
   works.
7. Edit form loads the stored quantity. Updating rice to `0.149 kg` removes the
   recipe from strict suggestions and puts it in Almost There.
8. Pantry values survive process termination/relaunch, confirmed in both the UI
   and a copied on-device SQLite database. Recipe seeds remain at 18.
9. Imperial-unit and disabled-alert preferences survive process restart.
10. Both pantry rows can be deleted, the empty state returns, and deletion
    persists after another process restart.

The external UI harness was corrected for emulator text-entry duplication and
zero-height empty RecyclerViews omitted from the accessibility tree. Those were
harness issues; no application fixes were made. These are focused smoke checks,
not comprehensive testing across all Android versions or devices.
