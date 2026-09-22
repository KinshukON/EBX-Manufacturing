# ON EBX Manufacturing

Manufacturing reference implementation for ON EBX FastTrack.

## Modules

- `manufacturing-lib`: reusable Java services and UI components.
- `manufacturing-web`: EBX module packaging, schemas, web resources, and demo-data services.

## Build

The build requires Java 17 or later and access to the configured EBX Maven repository.

```bash
mvn clean package
```

The project can also be included as the `manufacturing` module in the ON EBX FastTrack Maven reactor.
