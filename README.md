# EBX Manufacturing

Manufacturing reference implementation for ON EBX FastTrack.

## Modules

- `manufacturing-lib`: reusable Java services and UI components.
- `manufacturing-web`: EBX module packaging, schemas, web resources, and demo-data services.

## Build

The build requires an EBX server library directory containing `ebx-lib.jar` and a JDK
compatible with that runtime library. The current EBX 6.3 workspace library requires
JDK 21 or later; this module retains Java 17 source and bytecode compatibility.

```bash
export EBX_LIB_DIR=/path/to/ebx-server/lib
mvn clean package
```

The project can also be included as the `EBX-Manufacturing` module in the ON EBX FastTrack Maven reactor.

## Automatic startup

The module targets the workspace EBX 6.3 runtime. Deploy Reference Data first:
`CommonReferenceData/CommonReferenceData`, `Geographies/Geographies`, and
`UOM/UOM` must be present. Manufacturing checks these foundations, creates its
registered datasets without demo records, and installs the active
`ebx-perspective-manufacturing-01` perspective with Supplier Master as its landing page.
Startup retries up to ten times at 30-second intervals when dependencies are unavailable.
Bootstrap creates all target dataspaces before compiling any Manufacturing schema,
then refreshes cached models outside a transaction and checks dataset availability.
Its registry includes the transitive Product, Product Variant, Work Order Reference
and SAP Material dependencies required by the baseline models.

The canonical menu is
`manufacturing-web/src/main/resources/perspective/manufacturing.xml`.
Its four sections contain twelve working dataset/table actions. It is curated from
`data/Perspective_menu_correct.xml`; archived exports in `data/` are not packaged.
MCP Server, EBX Forge, banking, AI-module actions and media views are excluded.
Icons are packaged by this module.

Existing datasets and perspective edits are preserved. A dataset using a different
schema produces an explicit migration error instead of being deleted. Set
`-Debx.manufacturing.perspective=false` to disable automatic bootstrap and perspective
installation. The separate demo-seeder action remains explicit and retains legacy sample
content; it is not part of startup.
The initial three staging menu actions receive missing table selectors on upgrade
only when their branch/dataset still match the generated defaults.

Run `python3 scripts/verify_perspective.py` to verify navigation against the bootstrap
registry, real table names and packaged icons. Runtime installation and restart checks
also require an operational EBX repository.
