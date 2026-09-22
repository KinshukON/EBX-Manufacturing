# Antigravity Master Execution Prompt

## Manufacturing Supplier 360 Enhancement, Change Request Integration, and Omnicom Global Demo Seeding

You are enhancing the existing ON EBX Manufacturing and ReferenceData accelerators. Execute this directive surgically, phase by phase. Do not perform a broad rewrite. Do not invent repository paths, EBX APIs, table paths, workflow names, deployment coordinates, or capabilities.

## 0. Non-negotiable architectural decisions

1. Preserve all existing Omnicom/media demonstration data in the current Manufacturing Supplier dataset and current deployed parent dataspace. Do not create an Omnicom-specific dataspace or dataset. Demo-data contamination is accepted for this implementation.
2. Preserve the Change Request framework's isolated-authoring design. Each Supplier Change Request must create its normal child dataspace from the governed parent baseline. This child dataspace exists because of the Change Request lifecycle, not because data is Omnicom-specific.
3. Draft supplier changes occur in the Change Request child dataspace. Compare baseline versus proposed state there. Approval merges/publishes the authorized changes into the parent dataspace. Rejection/cancellation follows the existing cleanup behavior.
4. Enhance the existing `MfgSupplierManagement` model. Do not replace it and do not create parallel Supplier, Site, Plant, Material, Country, Currency, Language, UOM, Source System, or Geography masters.
5. Treat the existing `Companies` model as the provisional canonical organization/legal-entity authority. `Supplier` is the role-specific supplier profile. First verify whether the repository contains a newer authoritative Party/Organization/Golden Business Entity model. If one exists, report the conflict and stop before choosing another authority.
6. Preserve current stable keys, existing records, screenshots, toolbars, APIs, workflow links, and demo behavior. Migrations must be backward compatible.
7. Keep Meta Platforms and its regional legal entities as organizations/suppliers where appropriate. Model Facebook, Instagram, WhatsApp, YouTube, Google Ads, Amazon Ads, Mercado Ads, and RappiAds as brands/platforms/channels or offerings unless the repository proves they are separately contracted legal entities. Do not flatten a brand into a legal entity merely because it appears on a media plan.
8. Never allow AI enrichment to silently overwrite steward-approved identity, tax, banking, sanctions, compliance, risk, or corporate-relationship data.
9. Never seed or log genuine secrets, real bank-account data, personal contact data, authentication tokens, or confidential Omnicom contract terms. Use obviously synthetic values for sensitive demo fields.
10. All seed records derived from public research must be marked `DEMO`, `PUBLIC_RESEARCH`, or the closest available equivalent. They are illustrative candidate counterparties, not a claim to be Omnicom's complete or confidential supplier ledger.

## 1. Execution protocol

For every phase:

1. Inspect before editing.
2. Cite exact repository-relative file paths and relevant schema/table paths in the phase report.
3. Record every touched file.
4. Make the smallest coherent change set.
5. Compile and run relevant tests before moving forward.
6. Do not suppress failures, weaken tests, or comment out validators to get a green build.
7. Make seeders idempotent: lookup by stable primary key; create if missing; update only demo-owned fields if present; never blindly insert duplicates; never delete user-managed records.
8. Keep a running `supplier_360_implementation_report.md` containing discovery evidence, decisions, field mappings, migrations, tests, remaining gaps, and rollback notes.
9. Commit nothing and push nothing unless explicitly instructed separately.
10. If a required API or framework is missing, stop that phase and report `BLOCKED` with evidence. Do not improvise an incompatible framework.

Use these phase statuses: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETE`, `BLOCKED`, `DEFERRED`.

---

# Phase 0 — Repository, deployment, and compatibility discovery

## Goal

Establish the verified implementation baseline before changing code.

## Actions

1. Locate the modules containing:
   - `MfgSupplierManagement.xsd`
   - `MfgSupplierReference.xsd`
   - `MfgSitePlantManagement.xsd`
   - `MfgMaterialManagement.xsd`
   - `SapSupplierSource.xsd`
   - `SapBusinessPartnerSource.xsd`
   - `Companies.xsd`
   - Geography, Currency, Language, UOM, CommonReferenceData, GLEIF schemas
   - Supplier companion files: `@customLayout`, `@assistant`, `@function`, `@permission`, `@businessObjects`, `@search`, toolbars
   - Module registration, seeders, services, UI services, workflow definitions
   - Request/Change Request schemas, Java services, workflow models, comparison logic, merge/publication logic, child-dataspace cleanup
   - MAME configuration and Supplier match policies
2. Read repository instructions and build configuration.
3. Identify current parent dataspace and datasets from runtime configuration or deployment code. Do not hardcode names from documentation without evidence.
4. Produce an authority matrix with:
   - concept
   - schema file
   - dataspace
   - dataset
   - table XPath
   - primary key
   - display field
   - current consumers
   - reuse mechanism
5. Produce an existing Supplier table/field matrix for all 14 verified Supplier tables.
6. Classify each required feature as `EXISTS`, `PARTIAL`, `MISSING`, or `CONFLICTING`.
7. Establish how cross-module references to `Companies` can be deployed safely. Verify whether direct cross-dataset FK is supported in the current deployment order. If not, propose a non-duplicating relationship mechanism and document it before implementation.
8. Run the unmodified baseline build and tests. Record exact commands and results.

## Mandatory checkpoint

Do not edit XSD, Java, workflow, or configuration files until the Phase 0 report is complete. If canonical legal-entity authority or Change Request APIs are ambiguous, stop and present the alternatives.

## Acceptance evidence

- Verified inventory with paths
- Authority/reuse matrix
- Deployment coordinates
- Baseline build result
- Exact Change Request lifecycle trace
- Exact MAME capability trace
- No source changes yet

---

# Phase 1 — Reference values and backward-compatible schema foundation

## Goal

Enrich the existing Supplier model without duplicating authorities or breaking existing Omnicom records.

## 1A. Reuse enterprise reference authorities

Use cross-dataset `osd:tableRef` or the repository's established pattern for verified authorities:

- Country, Business Region, Subdivision
- Currency
- Language/Locale
- UOM
- Source System
- Address Type
- Contact Type
- Identifier Type
- Relationship Type
- Document Type
- Lifecycle Status
- NAICS/SIC/GICS industry classification
- GPC/product taxonomy
- GLEIF legal-entity enrichment

Do not create local duplicates. For Payment Terms, Incoterms, Certification Type, Risk Type/Rating, Supplier Site Role, Assignment Type, Compliance Requirement, Performance Metric, and Corrective Action Status, first search for an enterprise authority. If none exists, add a clearly scoped reusable table to the most appropriate existing Supplier Reference schema. Do not use SAP staging tables as the canonical vendor-neutral authority.

## 1B. Enhance `/root/Supplier`

Preserve every existing field and primary key. Add only missing fields, using repository conventions:

- `legalEntityId`: reference to canonical Companies/Organization authority
- `supplierLifecycleStatus`
- `onboardingStatus`
- `supplierClassification`
- `directIndirectType`
- `strategicSupplier`
- `criticalSupplier`
- `preferredSupplier`
- `diversityClassification`
- `primaryCurrency`
- `defaultPaymentTerms`
- `defaultIncoterms`
- `defaultLeadTimeDays`
- `riskTier`
- `complianceStatus`
- `dataQualityScore`
- `onboardingDate`, `activationDate`, `deactivationDate`, `deactivationReason`
- `lastReviewDate`, `nextReviewDate`
- `supplierOwner`, `dataSteward`
- `sourceOfTruth`, `matchConfidence`, `survivorshipStatus`, `lastVerifiedDate`
- `demoDataFlag`, `demoProfile`, `provenanceSource`, `provenanceAsOfDate`

Do not duplicate legal name, DUNS, LEI, tax identity, and corporate parent fields unnecessarily. Where backward compatibility requires retaining existing Supplier identity fields, designate them as cached/source values and define synchronization/survivorship rules.

Lifecycle target:

`PROSPECT -> ONBOARDING -> UNDER_REVIEW -> APPROVED -> ACTIVE -> SUSPENDED -> INACTIVE -> DEACTIVATED`

Reactivation is governed; it is not an unrestricted status edit.

## 1C. Enrich existing child tables

### SupplierIdentifier

Add identifier type, issuing country/authority, value, source system, verification status/date, effective dates, primary flag, confidence, and provenance.

### SupplierAddress

Retain existing address text. Add address lines, city, subdivision, postal code, country, latitude/longitude, address type, primary/remit-to/ordering flags, verification status, effective dates, and provenance.

### SupplierContact

Add contact type, site, title, department, email, phone, preferred language, primary/emergency flags, privacy/consent indicator, effective dates. Seed only fictional persons and example-domain email addresses.

### SupplierSite

Convert/enrich it into the Supplier-to-authoritative-Site assignment. Add `siteId` FK, relationship/role type, supplier site code, primary flag, ordering/remit-to/manufacturing flags, status, valid dates, and source reference. Keep legacy site name as a compatibility/display field if required.

### SupplierMaterial and SupplierMaterialIdentifier

Retain the existing Material FK. Add supplier part number, manufacturer role, approved status, applicable site/plant, UOM, capacity, minimum order quantity, lead time, preferred rank, valid dates, and source reference.

### SupplierQualification

Add qualification type, scope, assessor, assessment date, expiry/review date, decision, reason, evidence/document reference, and status.

### SupplierCertification

Add certification type, issuing authority, certificate number, scope, applicable site, issue/expiry dates, verification status, verified by/on, document reference, and renewal status.

### SupplierRiskProfile

Do not treat it as one timeless label. Add assessment ID/version, assessment date, risk type, inherent score, control score, residual score, rating, source, observation date, confidence, expiry/review date, assessor, rationale, and status.

### SupplierCapability

Add capability type, category/taxonomy, description, geography/site coverage, capacity, UOM, minimum/maximum volume, lead time, preferred flag, and effective dates.

### SupplierHierarchy

Preserve current records, but constrain hierarchy type so corporate ownership, operational supplier hierarchy, procurement classification, and media/channel taxonomy cannot be accidentally mixed. Add relationship source, confidence, verified status, ownership percentage where applicable, and effective dates.

### SupplierContractReference and SupplierSourceReference

Add contract type/status, governing entity, effective dates, source-system scope, external supplier/vendor ID, company code, purchasing organization, and provenance where appropriate. Do not seed confidential commercial terms.

## 1D. Add genuinely missing tables

Only after verifying they do not already exist, add:

1. `SupplierEnterpriseAssignment`
   - assignment ID, Supplier FK
   - internal legal entity, business unit, company code
   - purchasing organization, procurement organization
   - authoritative Site/Plant FK, cost center
   - source system, ERP vendor number, supplier account group
   - purchasing status
   - payment terms/currency/Incoterms/lead-time overrides
   - valid dates, assignment status

2. `SupplierBankAccount`
   - synthetic demo-safe account only
   - supplier, account holder, bank name/country, currency
   - masked account reference; never store/log plaintext secrets
   - IBAN/BIC/routing fields only if protected according to platform conventions
   - remit purpose, primary flag, verification status/method/by/on
   - effective dates, change-risk score
   - field/table permissions for least privilege

3. `SupplierBusinessContinuityPlan`
   - supplier/site, plan status, plan owner
   - critical services, alternate site/provider
   - RTO/RPO, last test date, next test date
   - document reference, findings, residual risk, effective dates

4. `SupplierIncident`
   - supplier/site, type, severity, opened/resolved dates
   - description, impact, owner, status, root cause, corrective action link

5. `SupplierPerformanceAssessment`
   - supplier/site/assignment, period
   - on-time delivery, quality/defect rate, fill rate
   - lead-time adherence, service level, invoice accuracy
   - compliance, responsiveness, overall score, trend
   - reviewer and corrective-action indicator

6. `SupplierCorrectiveAction`
   - assessment/incident link, action, owner, due date, status, evidence, closure date

7. `OrganizationRelationship` if no equivalent exists
   - from organization, relationship type, to organization
   - ownership percentage, control type, source, confidence
   - verified status and valid dates

8. `SupplierDuplicateCandidate` and `SupplierMatchDecision` only if MAME does not already persist equivalent auditable structures.

## Phase 1 rules

- Use additive, backward-compatible migrations.
- Add constraints progressively; do not make `legalEntityId` mandatory until existing suppliers have been linked or explicitly exempted.
- Preserve stable IDs and all Omnicom records.
- Validate every FK target and dataset coordinate.
- Generate or update SSD/data model documentation if that is the repository convention.

## Acceptance evidence

- XSD/schema validation passes
- Module builds
- Existing records remain readable
- No duplicate authority tables
- All new relationships resolve
- Permission design documented

---

# Phase 2 — Idempotent Supplier 360 demonstration seeding

## Goal

Seed a coherent, cross-regional Omnicom supplier ecosystem in the existing Manufacturing dataset. Do not create a separate Omnicom dataspace or dataset.

## 2A. Seeder behavior

Enhance the existing demo seeder or create a clearly named additional seeder within the same module, following the module's service pattern. It may be selectable by profile, but all records are written to the current Manufacturing parent dataset.

Required behavior:

- stable human-readable IDs
- idempotent create/update
- never delete current Omnicom records
- reuse/link existing Meta Platforms, Facebook Ads, Instagram Ads, YouTube Ads and related records
- if a legacy brand is currently stored as a Supplier, preserve it and add the correct relationship/classification; do not destructively re-key it
- resolve dependencies in order: reference values -> Companies/organizations -> relationships -> Suppliers -> sites -> assignments -> identifiers -> contacts -> capabilities -> qualifications -> certifications -> risks/continuity -> performance -> duplicates -> change-request scenario prerequisites
- log counts for created, reused, updated, skipped, and failed records by table
- transaction boundaries must prevent half-created relationship graphs
- rerun produces zero duplicates
- add a dry-run mode if the repository's seeder framework supports it

## 2B. Common reference values

Seed only values missing from authoritative reference tables and use stable codes:

- Supplier types: `MEDIA_OWNER`, `MEDIA_PLATFORM`, `TECHNOLOGY`, `CLOUD_INFRASTRUCTURE`, `DATA_IDENTITY`, `ADTECH_DSP`, `RETAIL_MEDIA_NETWORK`, `STREAMING_BROADCAST`, `PRODUCTION_CONTENT`, `RESEARCH_ANALYTICS`, `FACILITIES`, `PROFESSIONAL_SERVICES`
- Relationship types: `ULTIMATE_PARENT`, `SUBSIDIARY`, `BRAND_OF`, `PLATFORM_OF`, `OPERATES_IN_REGION`, `AUTHORIZED_RESELLER`, `DATA_PARTNER`, `TECHNOLOGY_PARTNER`, `MEDIA_INVENTORY_PROVIDER`, `SERVICE_PROVIDER`
- Regions: use authoritative North America, EMEA, APAC, LATAM records
- Lifecycle/status values required by Phase 1
- Risk types: financial, cyber, privacy, sanctions, geopolitical, operational, concentration, ESG, continuity
- Certification/compliance types: Supplier Code acknowledgement, Data Privacy, Information Security, Anti-Bribery, Modern Slavery/Human Rights, Health and Safety, ISO 27001, SOC 2
- Payment-term/Incoterm demo values only if an enterprise authority was not found

Do not remove `MEDIA_SUPPLIER`, `MEDIA_ADVERTISER`, or other legacy Omnicom values. Map or alias them if needed.

## 2C. Organizations and regional legal entities

Create or reuse Companies/Organization records. Use known legal names only where supported by existing seed data or verified repository content. Where legal-entity precision is uncertain, use `DEMO-CANDIDATE` in provenance and do not invent tax IDs, registration IDs, LEIs, or DUNS.

Seed these global parents/counterparties:

- Alphabet/Google
- Meta Platforms
- Amazon
- Microsoft
- Adobe
- Salesforce
- ByteDance
- Tencent
- Alibaba
- Baidu
- Netflix
- The Walt Disney Company
- Warner Bros. Discovery
- Paramount
- Walmart
- Mercado Libre
- Rappi
- The Trade Desk
- LiveRamp
- InfoSum
- Eyeota
- Tealium
- Sprinklr
- Naver
- Kakao
- LY Corporation/LINE, using the correct model available in repository
- Rakuten
- ProSiebenSat.1
- Globo
- ITV
- Instacart
- Anthropic
- OpenAI
- Outpromo
- Global Shopper
- Billion Dollar Boy
- Fabulate
- Insider
- Omnichat
- Acxiom, clearly flagged as affiliated/internal after ownership verification rather than blindly treated as an external supplier

Create regional legal-entity children only where the name is already supported or can be marked safely as a demonstration candidate. At minimum demonstrate:

- Google global parent plus US, Ireland/EMEA, and Japan/APAC billing-entity examples
- Meta Platforms global/US plus Ireland/EMEA billing-entity example
- Amazon global/US plus EU billing-entity example
- Microsoft and Adobe global parents with regional operating/billing examples only when verified

Do not seed fabricated tax IDs. Use synthetic source identifiers such as `DEMO-OMC-GOOGLE-EMEA-001`.

## 2D. Brands, platforms, channels, and offerings

Create/reuse and relate these to their legal parent rather than automatically treating all as separate legal entities:

- Google, YouTube, Google Ads, Google DV360, Google Cloud
- Facebook, Instagram, WhatsApp, Meta Ads
- AWS, Amazon Ads, Amazon DSP
- Microsoft Azure
- Adobe Workfront, Experience Manager, Firefly, Journey Optimizer
- TikTok and Douyin under ByteDance
- WeChat and QQ under Tencent
- Taobao and Tmall under Alibaba
- Mercado Ads under Mercado Libre
- RappiAds under Rappi
- Walmart Connect under Walmart
- Naver, Kakao, LINE, Rakuten media/commerce offerings

If the current schema lacks a Brand/Platform/Offering structure, use the enhanced SupplierCapability or an existing Product/Service/Channel reference model. Do not create duplicate Supplier legal entities simply to render the graph.

## 2E. Regional supplier coverage and assignments

Create demonstration coverage/assignment records:

### North America

- Google/YouTube, Meta/Facebook/Instagram/WhatsApp, Amazon/AWS/Amazon Ads, Microsoft Azure, Adobe, Salesforce
- Netflix, Disney, Warner Bros. Discovery, Paramount
- Walmart Connect, Instacart
- LiveRamp, InfoSum, The Trade Desk, Tealium, Sprinklr

### EMEA

- Google Ireland example, Meta Platforms Ireland example, Amazon EU example
- ITV, ProSiebenSat.1
- Adobe/Microsoft/Salesforce regional enterprise services
- GDPR/privacy requirement assignment

### APAC

- Tencent/WeChat/QQ
- ByteDance/Douyin/TikTok
- Alibaba/Taobao/Tmall
- Baidu
- Naver, Kakao, LINE, Rakuten
- Eyeota, Fabulate, Billion Dollar Boy, Omnichat
- Google Japan example where applicable

### LATAM

- Meta/Facebook/Instagram/WhatsApp
- Google/YouTube
- Mercado Libre/Mercado Ads
- Rappi/RappiAds
- Globo
- Outpromo, Global Shopper, Insider

Represent these as demo coverage, capability, relationship, and enterprise-assignment records. Do not assert exact contractual spend or confidential status.

## 2F. Sites and contacts

Reuse authoritative Site/Plant records. Create four demo procurement hubs only if equivalent sites do not exist:

- New York — North America/global procurement hub
- London — EMEA procurement hub
- Singapore — APAC procurement hub
- Sao Paulo — LATAM procurement hub

Use fictional contacts:

- `Alex Morgan`, Global Media Procurement, `alex.morgan@example.com`
- `Priya Sen`, APAC Supplier Steward, `priya.sen@example.com`
- `Sofia Costa`, LATAM Procurement Lead, `sofia.costa@example.com`
- `Daniel Reed`, EMEA Compliance Reviewer, `daniel.reed@example.com`

Mark all as synthetic demo data.

## 2G. Compliance, risk, continuity, and performance scenarios

Seed a varied but coherent story:

1. Meta Platforms: active, strategic, critical; privacy and concentration risks; approved Supplier Code acknowledgement.
2. Google/YouTube: active, strategic, critical; data privacy and concentration risks; performance score above target.
3. ByteDance/APAC: under review; geopolitical/privacy assessment requiring enhanced approval.
4. Mercado Libre/LATAM: active retail-media supplier; operational risk moderate; strong performance trend.
5. Rappi/LATAM: onboarding; continuity plan requires remediation.
6. Adobe: active technology supplier; ISO 27001 demo certification approaching expiry within 60 days.
7. A fictional production supplier: one expired insurance/compliance item and corrective action overdue.
8. A fictional critical production supplier: alternate site, RTO 24 hours, continuity test passed.

Use dates relative to seeding time when supported, otherwise deterministic dates documented in the report. Never imply that the synthetic risk rating is a real assessment of the named company; label it `DEMO SCENARIO`.

## 2H. Duplicate-prevention scenarios

Seed candidate pairs without corrupting golden data:

- `Meta Platforms, Inc.` versus `Facebook, Inc.`/legacy source name
- `Google LLC` versus `Google Inc.`/legacy source name
- `Amazon Advertising` versus `Amazon Ads` brand alias
- fictional `Global Creative Production Ltd` versus `Global Creative Productions Limited`

Expected decisions:

- legacy legal name -> link/alias to existing organization
- brand alias -> classify as brand/offering, not a duplicate supplier merge
- true fictional duplicate -> steward merge candidate

Persist match evidence: normalized name, domain/address/identifier signals, score, explanation, decision, steward, decision date, and audit trail.

## 2I. Bank-change scenario

Create one synthetic, masked bank account for a fictional supplier. Create a proposed bank-change demonstration that requires elevated approval. Do not apply the proposed account to the approved parent record until workflow approval. Never seed real routing/account numbers.

## Acceptance evidence

- First run counts by table
- Second run creates zero duplicates
- Existing Omnicom records preserved
- All FKs resolve
- Parent/brand/regional relationships render coherently
- Demo/provenance flags visible
- No real secrets or confidential commercial terms

---

# Phase 3 — Change Request integration with child dataspaces

## Goal

Reuse the existing Change Request framework as the outer governance layer for Supplier changes.

## Required services/workflows

- Onboard Supplier
- Extend Existing Supplier
- Update Supplier
- Change Supplier Bank Account
- Deactivate Supplier
- Reactivate Supplier

## Required lifecycle

1. User opens Requests.
2. User selects `Create Supplier Change Request`.
3. Framework creates Change Request and child dataspace from current governed parent baseline using the existing standard mechanism.
4. Request stores governing dataspace/dataset, child dataspace, request type, target supplier if any, owner, dates, status, risk flags, and description.
5. For onboarding, run the creation gate before creating the Supplier record.
6. User edits/enriches Supplier 360 in the child dataspace.
7. System validates identity, references, mandatory data, duplicate risk, compliance, certification, bank-change rules, and assignment integrity.
8. Compare view displays baseline versus proposed record and child-table changes.
9. Submit routes approval by risk.
10. Approval merges/publishes the authorized child-dataspace changes into the parent using existing framework APIs.
11. Rejection returns for rework or closes according to current framework.
12. Cancellation/closure invokes the existing safe child-dataspace cleanup policy.

Do not replace the framework with status fields in a shared table. Do not manually emulate branching if the framework already provides APIs.

## Creation gate

Before new Supplier creation, search canonical Companies, Supplier, identifiers, source references, and MAME candidates. Present:

- create a new legal entity and Supplier
- attach a Supplier profile to an existing Company
- extend an existing Supplier to another region/site/company code/purchasing organization
- request duplicate exception with justification

High-confidence duplicates block ordinary creation. Medium-confidence candidates warn and require disposition. Brand aliases must not be merged into legal entities.

## Risk routing

- ordinary descriptive/contact update: standard approval
- new supplier or new regional assignment: procurement + data stewardship
- bank, tax ID, legal identity, ultimate parent, sanctions/compliance override: elevated approval
- high-risk or critical supplier: risk/compliance approval
- fast track may bypass only stages explicitly permitted by current framework; never bypass bank, tax, sanctions, or high-confidence duplicate controls

## Acceptance evidence

- Child dataspace is created by the real framework
- Parent baseline remains unchanged before approval
- Compare view includes root and child-table changes
- Approval updates parent correctly
- Rejection/cancellation behaves safely
- Audit links request, workflow, dataspace, user, and changed records

---

# Phase 4 — AI enrichment and MAME duplicate governance

## Goal

Enhance—not fake—the existing AI/MAME patterns.

## AI policy

- Descriptions, standardized names, category suggestions, and non-sensitive classifications may be proposed.
- Legal identity, DUNS/LEI/tax, parent relationships, risk, and compliance require steward acceptance.
- Banking, sanctions, and compliance status may never be silently overwritten.
- Every suggestion stores source, retrieval date, confidence, proposed value, current value, disposition, and accepting user/date.
- External providers must be invoked only when configured. Otherwise use clearly labeled deterministic demo responses.

## MAME work

1. Verify actual Supplier match policy.
2. Configure fields appropriate to the available engine: normalized legal/trading name, DUNS, LEI, tax ID, registration ID, domain, country/address, phone, source IDs.
3. Weight exact authoritative IDs higher than fuzzy names.
4. Persist/reuse candidate clusters and steward decisions.
5. Support `MERGE`, `LINK_AS_ALIAS`, `LINK_AS_BRAND`, `EXTEND_EXISTING`, `LEGITIMATE_DUPLICATE`, `DISMISS`, `ESCALATE`.
6. Preserve source references and lineage after merge.
7. Never auto-merge solely from an LLM judgment.

## Acceptance evidence

- Deterministic test cases for exact, fuzzy, brand-alias, regional-entity, and legitimate-duplicate scenarios
- No silent sensitive-field mutation
- Full disposition audit

---

# Phase 5 — Supplier 360 user experience

## Goal

Implement a usable Supplier 360 with declarative configuration first and targeted Java UI only where required.

## Tabs

1. Overview — identity, lifecycle, DQ/compliance/risk scorecards, identifiers, primary contact/site
2. Organization — canonical legal entity, regional entities, parent hierarchy, relationship graph, source identities
3. Sites & Assignments — Supplier-to-Site, regions, company codes, purchasing organizations, plants, cost centers
4. Products & Services — capabilities, media/platform offerings, materials, lead times
5. Financial — terms, currencies, tax details, masked banking data with restricted permissions
6. Compliance & Risk — qualifications, certifications, assessments, continuity plans, incidents
7. Performance — period scorecards and corrective actions
8. Governance — Change Requests, approvals, duplicate decisions, AI suggestions, lineage, history

## Interaction requirements

- `Create Supplier Change Request` action
- `Extend Existing Supplier` action
- `AI Enrich` action with review-before-apply
- `Find Duplicates` action
- `Compare to Baseline` action when inside a Change Request child dataspace
- relationship graph differentiating legal entity, regional entity, brand/platform, and supplier relationship by shape/color/legend
- sensitive financial fields masked and permission controlled
- no broken links outside workflow context

Attempt `@customLayout` and existing declarative capabilities first. If graph, scorecards, or compare panel cannot meet requirements, implement a focused Java UI service. Document the exact declarative limitation; do not rebuild the entire form in Java.

## Acceptance evidence

- screenshots or rendered verification for all tabs
- permission tests by role
- graph semantics correct
- no empty decorative tabs
- responsive behavior verified in supported EBX UI

---

# Phase 6 — Validation, security, permissions, and data quality

## Validations

- Supplier must link to a canonical legal entity before approval, except documented legacy exemption
- one active primary address/contact/site/bank account per applicable scope
- dates are chronological
- certification expiry/review alerts at 90/60/30 days
- score ranges and calculated overall scores are valid
- region/country/site relationships are consistent
- company code/purchasing organization/site assignments are not duplicated for overlapping dates
- inactive/deactivated supplier cannot receive a new active assignment without reactivation
- critical supplier requires risk and continuity review
- bank changes require elevated approval
- brands/platforms cannot be mistaken for legal entities without explicit override
- public-research/demo records are visibly labeled

## Roles

Map to existing roles where possible:

- Supplier Requester
- Supplier Data Steward
- Procurement Approver
- Risk/Compliance Approver
- Finance/Banking Approver
- Supplier Administrator
- Read-only Business User

Enforce least privilege at dataset/table/field/service/workflow levels. Include negative tests.

## DQ score

Implement a transparent score based on configured completeness, validity, consistency, duplicate risk, freshness, and provenance. Store component scores or make them explainable; do not show a magic number.

---

# Phase 7 — Integration, publication, regression, and documentation

## Integration

- Preserve SAP source/staging semantics.
- Map source vendor/business-partner IDs into SupplierSourceReference and assignments.
- Do not use SAP Payment Terms or Incoterms staging records as universal authorities without approved mapping.
- Publish only approved/active parent-dataspace state.
- Include enterprise Supplier ID, legal-entity ID, source IDs, assignment scope, lifecycle, and lineage.

## Regression suite

Test at minimum:

1. Existing Omnicom demo records still load.
2. Existing Company features still work.
3. Supplier seeder first and second run.
4. New Supplier onboarding from request creation through approval.
5. Extend existing supplier to a new regional assignment.
6. Duplicate block and exception route.
7. Brand alias is linked, not merged as a legal duplicate.
8. AI suggestion accept/reject.
9. Bank-change elevated approval.
10. Certification-expiry warning.
11. Deactivate/reactivate lifecycle.
12. Compare root plus child changes.
13. Parent unchanged before approval.
14. Rejected/cancelled child dataspace handling.
15. Permissions and masked fields.
16. API/MCP backward compatibility.

## Final deliverables

- implemented source changes
- `supplier_360_implementation_report.md`
- `supplier_360_authority_matrix.md`
- `supplier_360_schema_crosswalk.md`
- `supplier_360_seed_catalog.md` with every stable ID and relationship
- `supplier_360_test_evidence.md`
- migration and rollback instructions
- demo operator script

---

# Phase 8 — Demonstration-ready acceptance story

The completed implementation must support this exact story:

1. Open the governed parent Manufacturing Supplier dataset containing the existing Omnicom records.
2. Show Meta Platforms as the legal organization, its regional legal/billing entities where supported, and Facebook/Instagram/WhatsApp as brands/platforms—not duplicate legal suppliers.
3. Open Requests and create a Supplier Change Request.
4. Verify the framework creates an isolated child dataspace from the current baseline.
5. Attempt to onboard `Facebook Inc.` and show duplicate/legacy-name detection.
6. Choose `link to existing Meta organization` or `extend existing Supplier`, rather than creating an unnecessary golden supplier.
7. Add an APAC or LATAM assignment and relevant site/coverage.
8. Run AI enrichment; review source/confidence and accept only safe proposed values.
9. Add or review compliance, certification, risk, and continuity data.
10. Show the regional supplier ecosystem graph.
11. Show baseline-versus-proposed comparison, including child-table changes.
12. Submit for risk-based approval.
13. Verify the governed parent remains unchanged before approval.
14. Approve and merge/publish through the existing framework.
15. Verify the parent now contains the approved assignment and complete audit lineage.

## Final stop condition

The task is complete only when all implemented phases have evidence, the build/tests pass, the seeder is idempotent, existing Omnicom data remains in the same Manufacturing dataset, and Change Requests demonstrably continue to use isolated child dataspaces. If any item is not achieved, mark it explicitly `BLOCKED` or `DEFERRED`; do not describe it as complete.

