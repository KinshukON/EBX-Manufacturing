# EBX Manufacturing

Manufacturing reference implementation for ON EBX FastTrack.

## Modules

- `manufacturing-lib`: reusable Java services and UI components.
- `manufacturing-web`: EBX module packaging, schemas, web resources, and demo-data services.

## Build

The build requires Java 17 or later and an EBX server library directory containing `ebx-lib.jar`.

```bash
export EBX_LIB_DIR=/path/to/ebx-server/lib
mvn clean package
```

The project can also be included as the `EBX-Manufacturing` module in the ON EBX FastTrack Maven reactor.
