# Antigravity Execution Prompt — Supplier 360 Model Enhancement and Seed Data

## Mission

Enhance the existing Manufacturing Supplier Management model into a credible, extensible Supplier 360 foundation. Work surgically in the current repository. Extend verified structures; do not create parallel lookalike models.

This prompt covers only data model, reference reuse, validation, migration, seeding, and model-level tests. Do not implement the Supplier 360 layout in this run; that is governed by the separate layout prompt.

## Non-negotiable decisions

1. Keep all existing Omnicom demo data in the existing Manufacturing Supplier dataset and dataspace.
2. Do not create an Omnicom-specific dataspace or dataset.
3. Do not delete, relocate, rename, or silently reinterpret current Omnicom records.
4. Preserve Change Request isolation: each Change Request continues to create its normal child dataspace from the parent baseline.
5. The child dataspace exists for change governance, not for separating Omnicom data.
6. Preserve stable primary keys and backward compatibility wherever technically possible.
7. Seed data idempotently. A second run must create zero duplicates.
8. Never invent tax IDs, DUNS numbers, LEIs, bank-account numbers, spend, sanctions results, contracts, or regulatory facts.
9. Brands and channels may remain Supplier demo records for backward compatibility, but add explicit classification so they are not confused with verified legal entities.
10. Use EBX 6.2.3 APIs and schema syntax proven by repository code or supplied documentation. Never guess an API signature.

## Known baseline to verify

The repository is expected to contain:

- `MfgSupplierManagement.xsd`
- `MfgSupplierReference.xsd`
- `MfgSitePlantManagement.xsd`
- `MfgMaterialManagement.xsd`
- SAP supplier and business-partner source schemas
- `Companies.xsd` with `/root/Companies`
- ReferenceData authorities for Geography, Currency, Language, UOM, Source System, Address Type, Contact Type, Identifier Type, Relationship Type, Document Type, Lifecycle Status, and industry classifications
- Existing Supplier tables including Supplier, Identifier, Address, Contact, Hierarchy, Site, Capability, Qualification, Material, Material Identifier, Risk Profile, Certification, Contract Reference, and Source Reference
- Existing Omnicom/media records and a demo seeder

Treat this list as a discovery hypothesis. Report exact source paths, table paths, PKs, FKs, labels, and field names before changing anything.

# Phase M0 — Evidence gate and impact analysis

## Actions

1. Locate source files under `src/main`; do not edit generated `target` artifacts.
2. Inventory all tables and fields in the four core schemas.
3. Locate dataset and dataspace declarations and every cross-dataset `osd:tableRef` coordinate.
4. Locate the Supplier seeder, module registration, workflow declarations, MAME configuration, Change Request implementation, and migrations.
5. Search all Java, XML, XPath, toolbar, workflow, REST, MCP, tests, and documentation references to Supplier paths that may be affected.
6. Identify whether `/root/Companies` can be referenced cross-dataset from Supplier and record its actual deployment coordinates.
7. Classify every proposed item below as `EXISTS`, `PARTIAL`, `MISSING`, or `CONFLICTING`.
8. Produce an impact matrix: proposed change, existing path, consumers, compatibility risk, migration action, and test.

## Stop rule

Do not modify files until the evidence report is complete. Stop only if the canonical Companies authority or actual cross-dataset coordinates cannot be established safely.

# Phase M1 — Canonical identity and Supplier core

Retain `/root/Supplier` and its current PK. Enhance it as the supplier-specific profile/lifecycle record.

Add or verify these concepts using repository naming conventions:

- Immutable enterprise Supplier ID
- `legalEntityId` FK to canonical `/root/Companies`
- Record classification: legal entity, operating entity, brand, platform/channel, individual, government, or other
- Legal name and trading/display name
- Supplier type, lifecycle status, onboarding status, qualification status
- DUNS, LEI, tax identifier references only when verified or explicitly synthetic
- Country of registration and headquarters country
- Website, primary language, preferred currency
- Supplier tier, strategic flag, diversity status, criticality
- Primary contact and primary site references where circular dependency can be avoided
- Source confidence, verification status, last verified date, verified by
- Created/updated audit metadata using native EBX audit where available; do not duplicate native system columns without reason
- AI provenance fields only where the existing accelerator uses them consistently

## Canonical-identity rule

`Companies` is the legal-entity authority. Supplier-specific lifecycle and procurement attributes belong in Supplier. Do not copy the entire Companies structure into Supplier.

For existing Supplier rows without a Companies match:

- Do not fabricate a legal entity.
- Allow a controlled transitional state if mandatory linkage would break deployment.
- Add a validation warning and migration status.
- Seed verified Companies records first, then link eligible demo Suppliers.

# Phase M2 — Enrich existing related tables

Enhance rather than replace the existing tables.

## SupplierIdentifier

- Identifier type FK
- Identifier value
- Issuing country/authority
- Source system
- Effective and expiration dates
- Primary and verified flags
- Verification metadata
- Uniqueness by supplier + type + normalized value + source where appropriate

## SupplierAddress

- Address type FK
- Lines 1–3, city, subdivision, postal code, country
- Latitude/longitude and geocoding status
- Primary, remit-to, ordering, shipping, registered-office flags
- Effective dates and source
- Do not store every address only as one free-text field

## SupplierContact

- Contact type FK
- Given name, family name, display name, role/title
- Email, phone, mobile, preferred language
- Related site, primary flag, active flag, consent/contact restrictions where applicable
- Validate email format without inventing personal information

## SupplierSite

Convert this from a site-name placeholder into an assignment referencing the authoritative `/root/Site` in `MfgSitePlantManagement`.

- Supplier FK
- Site/Plant FK
- Relationship/assignment type
- Supplier site code and ERP vendor/site code
- Ordering, fulfillment, manufacturing, service, remit-to, and ship-from flags
- Effective dates, status, lead time, minimum order quantity, preferred flag
- Preserve legacy `siteName` temporarily only if required for migration

## SupplierMaterial and SupplierMaterialIdentifier

- Retain the existing Material FK
- Add supplier part number, manufacturer part number, UOM, currency, price-reference metadata, lead time, MOQ, approved/preferred status, validity dates, source, and site applicability
- Do not turn price-reference data into a purchasing transaction ledger

## SupplierQualification

- Qualification type/status
- Scope, assessor, assessment date, next review, score, decision rationale, evidence/document reference

## SupplierCertification

- Certification type, name, issuer, certificate number
- Issue, effective, expiration, verification dates
- Scope, covered sites, status, document reference, verified by
- Expiration and missing-evidence validations

## SupplierRiskProfile

- Risk type/category, inherent/residual rating, numeric score
- Assessment date, next review, source, assessor
- Indicator, rationale, mitigation, status, confidence
- Keep time history; do not overwrite every assessment into one current label

## SupplierCapability

- Capability category, description, geography/site scope, capacity or qualitative rating, effective dates, verification status

## SupplierHierarchy

Keep one relationship table only if it can clearly type and validate distinct semantics. At minimum support:

- Corporate ownership
- Supplier operational hierarchy
- Affiliate/partner relationship
- Brand/platform relationship
- Supply-chain relationship

Do not encode procurement category taxonomy as corporate ownership. Add relationship type, parent/child roles, effective dates, ownership percentage when verified, source, confidence, and cycle prevention.

## Contract and Source References

Add type, source-system FK, external ID, status, dates, URI/document reference, and verification/provenance without creating a contract-management system.

# Phase M3 — Add missing governed entities

Create only after proving no authoritative equivalent exists.

1. `SupplierEnterpriseAssignment`
   - Supplier, legal entity/company code, purchasing organization, plant/site, business unit, cost center, ERP vendor number, source system, status, validity dates, primary flag.
   - Use nullable scope dimensions with validations; do not generate a separate table for every ERP.

2. `SupplierBankAccount`
   - Supplier, account holder, bank name, country, currency, account type, masked account display, encrypted/tokenized sensitive reference, IBAN/BIC where permitted, remit-to site/address, verification status/date, validity, source.
   - Never seed real banking data. Store a clearly synthetic masked scenario.
   - Apply field-level permissions and prevent sensitive values from appearing in logs, exports, MCP, or ordinary search.

3. `SupplierPaymentProfile`
   - Supplier/assignment/site scope, payment terms, Incoterms and location, currency, payment method, lead time, validity, source.
   - Reuse enterprise authorities if present; otherwise introduce vendor-neutral reference tables, not SAP-shaped golden values.

4. `SupplierBusinessContinuityPlan`
   - Plan type, covered supplier/sites/capabilities, status, review/test dates, recovery objectives, alternate site/supplier, document reference, owner, assessment result.

5. `SupplierPerformanceAssessment`
   - Period, site/material/assignment scope, on-time delivery, defect/quality, invoice accuracy, service, composite score, source, reviewer.

6. `SupplierIncident`
   - Type, severity, dates, scope, description, impact, status, owner, root cause, resolution.

7. `SupplierCorrectiveAction`
   - Incident/assessment FK, action, owner, due date, completion date, status, evidence, effectiveness result.

8. `SupplierDocument`
   - Document type, title, reference/attachment, status, dates, confidentiality, related certification/risk/continuity item.

9. `SupplierDuplicateDecision`
   - Candidate/cluster IDs, compared suppliers, score, matched attributes, disposition, steward, rationale, timestamps, merge/survivorship reference.

10. `SupplierDataQualityAssessment`
    - Completeness, validity, consistency, uniqueness, timeliness, overall score, calculation date, failed rule count, rule summary.

# Phase M4 — Reference authority and validation layer

1. Reuse verified cross-dataset authorities for Country, Region/Subdivision, Currency, Language, UOM, Source System, Address Type, Contact Type, Identifier Type, Relationship Type, Document Type, Lifecycle Status, NAICS/SIC/GICS, and GPC.
2. Verify actual branch/dataset/table coordinates. Never copy coordinates from prose without repository evidence.
3. Where Payment Terms, Incoterms, Certification Type, Risk Type/Rating, Supplier Tier, Diversity Type, and Continuity Plan Type lack vendor-neutral authorities, add compact reference tables in the appropriate reference model.
4. Retain current Omnicom-specific reference values. Do not move them to another dataspace.
5. Add validations for required identity, date order, country-specific identifier shape only when authoritative, relationship cycles, duplicate primary records, score ranges, certification expiry, active assignment scope, bank masking, and approved-record completeness.
6. Use errors only for conditions that must block governance; use warnings for migration gaps and advisory quality issues.

# Phase M5 — Change Request compatibility

The model must work unchanged in the parent Manufacturing dataspace and in every Change Request child dataspace.

- Do not hard-code a dataspace key.
- Resolve the active repository/session context.
- Ensure cross-dataset references behave from child dataspaces.
- Ensure new tables participate in baseline/proposed comparison and merge.
- Parent baseline must remain unchanged before approval.
- Approval merges governed changes back using the existing framework.
- Rejection/cancellation leaves no approved-state mutation.
- Do not rewrite the Change Request framework unless a model compatibility defect is proven.

# Phase M6 — Idempotent demo seeding

Enhance the existing seeder; do not create a separate Omnicom dataspace or relocate records.

## Seeder engineering rules

- Seed in dependency order: reference values, Companies, Suppliers, identifiers, relationships, sites, assignments, materials/services, finance, compliance, risk, continuity, performance, duplicate scenarios.
- Use stable deterministic keys.
- Upsert by PK; verify natural keys before creation.
- Preserve user-edited values unless the field is explicitly seeder-owned.
- Emit created/updated/skipped/conflicted counts by table.
- Run twice in an automated test and prove the second execution creates zero rows.
- Never delete records absent from the seed catalog.

## Required demonstration network

Preserve all existing Meta Platforms, Facebook Ads, Instagram Ads, YouTube Ads, Media Supplier, Media Advertiser, media hierarchy, and taxonomy data. Enrich the cross-regional story with stable demo records for:

- Meta/Instagram/Facebook/WhatsApp ecosystem
- Alphabet/Google/YouTube/Google Ads/DV360 ecosystem
- Amazon/AWS/Amazon Ads ecosystem
- Adobe, Microsoft, Salesforce
- Tencent, ByteDance, Alibaba, Baidu
- Mercado Libre, Rappi, Globo
- Representative streaming, retail-media, data/identity, ad-tech, creative-production, and technology suppliers
- Procurement hubs/sites for New York, London, Singapore, and São Paulo
- North America, EMEA, APAC, and LATAM assignments

Classify each record as legal entity, operating entity, brand, platform/channel, or demo candidate. Link brands/platforms to legal entities when supportable; otherwise label the relationship as unverified demo context.

## Required scenario data

- Multiple addresses, contacts, sites, currencies, payment terms, and assignments
- One expiring certification
- One high-risk/critical supplier with mitigation
- One tested and one overdue continuity plan
- Performance history with good and deteriorating trends
- One incident and corrective action
- One deliberate near-duplicate pair plus persisted steward decision
- One synthetic masked bank-account change scenario
- One supplier serving multiple plants/company codes/purchasing organizations
- Source references for SAP and at least one non-SAP source, without inventing production credentials

# Phase M7 — Build, migration, and acceptance

1. Validate every changed XSD and companion configuration.
2. Compile the affected Maven modules from source.
3. Run unit/integration tests and add focused tests for PK/FK integrity, validations, cycle detection, security-sensitive serialization, and seeder idempotency.
4. Test upgrade against an existing dataset snapshot or representative fixture; no destructive reinitialization.
5. Verify existing Omnicom IDs and record counts remain intact except deliberate additive enrichment.
6. Verify child-dataspace create/edit/compare/approve/reject behavior.
7. Produce:
   - exact changed-file list;
   - before/after table-field matrix;
   - reference authority matrix;
   - migration notes;
   - seed manifest and counts;
   - test commands and results;
   - remaining risks or manual deployment steps.

## Completion condition

Stop only when source compilation succeeds, schemas validate, the second seeder run creates zero duplicates, existing Omnicom data remains in place, and the enhanced model works in both the parent dataspace and a Change Request child dataspace. Do not claim completion from code inspection alone.
