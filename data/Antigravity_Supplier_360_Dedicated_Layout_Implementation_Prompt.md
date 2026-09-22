# Antigravity Execution Prompt — Supplier 360 Layout and UI Implementation

## Mission

Implement a polished, usable Supplier 360 experience for `/root/Supplier` after the enhanced model is deployed. This prompt covers layout, navigation, UI services, role-aware actions, comparison, responsive behavior, and UI testing. Do not redesign the schema in this run. If a required path is missing, report it as a model prerequisite instead of inventing a parallel field.

## Non-negotiable decisions

1. Target the existing `MfgSupplierManagement` dataset in its current Manufacturing dataspace.
2. Omnicom demo data remains visible in that same dataset; do not create an Omnicom dataspace, dataset, or separate UI application.
3. Change Requests continue to use isolated child dataspaces. The UI must clearly show whether the user is viewing the parent baseline or a Change Request working branch.
4. Use declarative `MfgSupplierManagement_@customLayout.xml` first.
5. Use the supplied Companies custom layout as a proven syntax reference, not as the desired visual design.
6. Use targeted Java UI services only when a documented EBX 6.2.3 declarative limitation prevents the relationship graph, scorecards, or comparison experience.
7. Do not rebuild ordinary forms, tables, and associations in Java.
8. Respect EBX permissions, field masking, validation, locale, history, and workflow context.
9. Never expose unmasked bank data, secrets, tokens, or restricted tax information.
10. Do not guess component names or Java APIs. Prove them from repository examples or the EBX 6.2.3 documentation.

# Phase L0 — UI discovery and path contract

## Actions

1. Verify the deployed enhanced schema and enumerate exact paths for Supplier and all associated tables.
2. Inspect:
   - `MfgSupplierManagement_@customLayout.xml`
   - Companies `@customLayout` and related UI configuration
   - all non-empty accelerator layouts
   - toolbars, business objects, functions, search, assistant, permissions, navigation seeders, and UI services
   - Change Request compare implementation
   - hierarchy/graph renderers available in the repository
3. Confirm which declarative imports are supported: `@layout`, `@tab-pane`, `@tab`, `@grid`, `@grid-element`, `@form`, table/association components, labels, conditional visibility, actions, and reusable components.
4. Create a UI path contract mapping every visible widget to a verified schema path or service.
5. Classify each requirement as `DECLARATIVE`, `EXISTING_SERVICE_REUSE`, `TARGETED_JAVA_REQUIRED`, or `BLOCKED_BY_MODEL`.

## Mandatory checkpoint

Do not write layout or Java until the path contract and implementation classification are complete. If the model prompt has not been applied, stop with the missing-path list.

# Phase L1 — Information architecture and page shell

Create a Supplier 360 record layout with:

- Existing/default toolbar plus authorized Supplier actions
- Persistent header/summary band
- Eight tabs
- Clear read/edit state
- Dataspace context badge: `Approved Baseline` or `Change Request Draft`
- Change Request ID/status/owner when in a governed child dataspace
- Validation summary and unsaved-change feedback

## Header band

Display, subject to verified paths:

- Supplier display/legal name
- Supplier ID
- Record classification
- Lifecycle and onboarding status
- Supplier type/tier
- Primary country/site
- DQ score
- Risk rating
- Qualification/compliance indicator
- Last verified/updated date
- Source-system identity summary

Provide actions only when authorized:

- Create Change Request
- Open active Change Request
- Enrich with AI
- Check duplicates
- Compare to baseline
- Submit for approval
- Approve/reject when assigned
- View history/lineage

Do not display an action that will inevitably fail in the current context.

# Phase L2 — Declarative eight-tab Supplier 360

Implement tabs in this exact order.

## 1. Overview

- Identity card: legal/display name, IDs, classification, type, tier, industry, registration geography, website
- Lifecycle card: onboarding, qualification, active/inactive, effective dates, ownership/steward
- Scorecards: DQ, compliance, risk, performance using accessible status text plus color
- Key identifiers table
- Primary contact card
- Primary site/address card
- Source and verification summary
- Short alert strip for blocking validations, expiring certification, high risk, possible duplicate, or pending request

## 2. Organization

- Canonical Companies/legal-entity link
- Parent, ultimate parent, subsidiaries/affiliates
- Typed organization relationships
- Brand/platform relationships clearly distinguished from legal ownership
- Source-system identities
- Embedded relationship graph if declaratively supported; otherwise a focused `Open Relationship Graph` panel/service
- Effective dates, source, confidence, and verified/unverified state on relationships

## 3. Sites & Assignments

- Supplier sites linked to authoritative Site/Plant records
- Addresses and contacts by site
- Enterprise assignment table for legal entity, company code, purchasing organization, plant, business unit, cost center, ERP vendor number
- Assignment status/effective dates
- Lead time, MOQ, ordering/ship-from/service flags
- Filters for region, site, status, and source

## 4. Products & Services

- Capabilities/categories and offerings
- Supplier-Material relationships
- Supplier/manufacturer part numbers
- UOM, currency, lead time, MOQ, approved/preferred status
- Site applicability and validity dates
- Do not display procurement prices as if they were transactional spend

## 5. Financial

- Payment profile, currency, payment terms, Incoterms/location, payment method
- Remit-to addresses/sites
- Bank-account list with masked display only
- Verification status and effective dates
- Tax identifier summary only for roles permitted to see it
- Bank changes highlighted as governed/high-risk changes
- Never place full sensitive values in HTML attributes, logs, client-side state, URL parameters, exports, or tooltips

## 6. Compliance & Risk

- Certifications with status, issuer, number, scope, issue/expiry dates, verification, and documents
- Risk assessments/history and mitigations
- Business continuity plans, tests, review dates, alternate sites/suppliers
- Sanctions/watchlist indicators only when backed by real source results; otherwise show `Not assessed`, not `Clear`
- Incidents and corrective actions
- Expiry and overdue alerts

## 7. Performance

- KPI history: on-time delivery, defects/quality, invoice accuracy, service and composite score
- Trend visualization only if accurate and accessible; otherwise use a compact history table
- Performance assessments by period/site/material
- Incidents and corrective actions
- Avoid implying operational telemetry is mastered when it is only demo or sourced summary data

## 8. Governance

- Active and historical Change Requests
- Workflow status, current task, requester, owner, approvers
- Baseline-versus-proposed comparison
- Comments and approval decisions
- Duplicate candidates and steward decisions
- Source lineage and cross-references
- EBX audit/history entry point
- Publication/downstream status when available

# Phase L3 — Reusable declarative components

Build reusable components in the custom layout where supported, following the Companies `FlatGroup` pattern only as a syntax precedent.

Suggested components:

- `SupplierSummaryHeader`
- `StatusBadge`
- `ScorecardTile`
- `AlertStrip`
- `SectionCard`
- `AssociationTable`
- `EmptyState`
- `MaskedValue`
- `DataspaceContext`

Requirements:

- No duplicated giant expression fragments when a reusable component is viable.
- Use node labels and localization rather than hard-coded technical paths in the visible UI.
- Provide meaningful empty states such as `No certifications recorded`, with an Add action only when authorized.
- Keep the layout renderable if optional associations contain no rows.
- Preserve native EBX validation messages and edit controls.

# Phase L4 — Relationship graph

Implement only after proving declarative hierarchy/relationship rendering is insufficient.

The graph must:

- Start from the current Supplier/legal entity
- Distinguish legal entities, operating entities, brands/platforms, sites, and supply-chain partners
- Use edge type for ownership, affiliate, brand/platform, supplier hierarchy, or supply-chain relationship
- Support expand/collapse and depth limits
- Open a selected node's detail in an in-page right-side pane when supported safely; otherwise open the native record view
- Display source, confidence, and effective dates
- Prevent cycles from freezing rendering
- Respect record permissions and omit inaccessible nodes/fields
- Offer a table fallback for accessibility and large graphs

Do not build a generic graph platform. Implement the smallest focused service necessary.

# Phase L5 — Scorecards and visual indicators

Use model-backed values only.

- DQ: completeness, validity, consistency, uniqueness, timeliness
- Risk: current rating/score and last assessment
- Compliance: active/expiring/expired/missing evidence
- Performance: latest composite and trend

Every color indicator must also include text/icon meaning. Define thresholds in reference/configuration or service logic with tests; do not bury unexplained magic numbers in the layout expression.

# Phase L6 — Change Request comparison experience

Reuse the existing Change Request framework and active child-dataspace context.

Required behavior:

1. In parent baseline, show active/pending requests affecting the Supplier.
2. In child dataspace, show a persistent `Change Request Draft` indicator.
3. Compare parent baseline with the current child branch across Supplier and all governed child tables.
4. Group differences by the same eight UI tabs.
5. Display Added, Changed, Removed, and Unchanged states.
6. Show old value, proposed value, source, editor, and timestamp where available.
7. Mask sensitive values in both old and proposed columns.
8. Provide navigation from a difference to the affected tab/record.
9. Parent data must not change before approval.
10. Submit/approve/reject actions must invoke existing workflow APIs, not direct status manipulation.

If a Java compare service is required, make it read-only and repository-context aware. Do not hard-code dataspace names.

# Phase L7 — Creation and duplicate gate

Enhance navigation/toolbars so `Create Supplier` launches the governed creation path.

- Search Companies and Suppliers before creation
- Run configured MAME matching when available
- Show possible matches with score and matched attributes
- Offer `Use existing supplier`, `Extend existing supplier`, `Open steward review`, or `Continue as new` according to threshold and permission
- High-confidence duplicate behavior must follow verified match policy; do not invent thresholds
- Persist duplicate decisions in the governed model
- Create the Supplier inside the Change Request child dataspace
- Do not bypass normal EBX validation or workflow

# Phase L8 — Role-aware behavior and security

Verify at least these personas using actual repository roles or map them explicitly:

- Requester/Business User
- Data Steward
- Risk/Compliance Reviewer
- Finance Reviewer
- Approver
- Read-only Consumer

Test:

- Tab/section visibility
- Field editability
- Action visibility and authorization
- Bank/tax masking
- Cross-dataset record access
- Child-dataspace access
- Direct URL/service invocation authorization, not only hidden buttons

# Phase L9 — Responsive, accessibility, and performance quality

- Desktop-first EBX layout with acceptable narrower-width behavior
- No horizontal page overflow from wide association tables; use local scrolling or column selection
- Keyboard-reachable tabs/actions
- Visible focus and accessible labels
- Color-independent status meaning
- Loading, empty, error, and permission-denied states
- Paginate or lazy-load large associations and graph expansions
- Do not execute one query per child row
- Cache only safe reference/display data and respect repository changes

# Phase L10 — Verification and handoff

1. Validate the custom-layout XML/expression with EBX tooling or deployment.
2. Compile affected Java modules.
3. Deploy and open at least:
   - one legal-entity supplier;
   - one brand/platform demo Supplier;
   - one multi-site/multi-assignment Supplier;
   - one high-risk/expiring-certification Supplier;
   - one duplicate scenario;
   - one Supplier in a Change Request child dataspace.
4. Test view and edit modes for each persona.
5. Test all eight tabs with populated and empty associations.
6. Test baseline comparison, submit, approve, reject, and post-approval parent view.
7. Test bank/tax masking in UI, compare, logs, export, and error paths.
8. Capture screenshots of the header, every tab, graph/table fallback, duplicate gate, and compare view.
9. Produce:
   - exact changed-file list;
   - UI path contract;
   - declarative-versus-Java decision log;
   - action/role matrix;
   - test evidence;
   - known limitations and follow-up items.

## Completion condition

Stop only when the Supplier 360 renders successfully from the source-deployed module, all eight tabs work, permissions and masking are verified, a Change Request draft can be compared to its parent baseline, and no existing Omnicom Supplier record becomes inaccessible. Do not claim completion from generated XML or compilation alone.
