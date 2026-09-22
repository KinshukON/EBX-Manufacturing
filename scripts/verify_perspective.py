#!/usr/bin/env python3
"""Validate the packaged navigation against real schemas, bootstrap registry and icons."""
import pathlib
import re
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parents[1]
WEB = ROOT / "manufacturing-web"
SCHEMAS = WEB / "src/main/webapp/WEB-INF/ebx/schemas"
JAVA = WEB / "src/main/java/com/onebx/ebx/fasttrack/manufacturing"
source = (JAVA / "service/DemoDataSeederService.java").read_text()
registry = {
    (branch, dataset): schema
    for branch, dataset, schema in re.findall(
        r'\{ "([^"]+)", "([^"]+)", "([^"]+\.xsd)"', source)
}
foundations = {
    ("Geographies", "Geographies"): "Geography.xsd",
    ("UOM", "UOM"): "UOM.xsd",
}
root = ET.parse(WEB / "src/main/resources/perspective/manufacturing.xml")
items = root.findall("./domain/menuItem")
ids = [item.findtext("id") for item in items]
assert len(ids) == len(set(ids)), "Duplicate menu identifiers"
assert "3046" in ids, "Default supplier action is missing"
assert len([item for item in items if item.findtext("type") == "section"]) == 4
for item in items:
    parent = item.findtext("parent")
    assert not parent or parent in ids, f"Unresolved parent: {parent}"
    icon = item.findtext("./icon/reference")
    if icon:
        assert icon.startswith("/ebx-manufacturing/")
        assert (WEB / "src/main/webapp" / icon.removeprefix("/ebx-manufacturing/")).is_file(), icon
    if item.findtext("type") != "action":
        continue
    params = {p.findtext("name"): p.findtext("value") for p in item.findall("./action/serviceParameters")}
    assert params.get("xpath"), "Perspective actions require an explicit table selector"
    key = (params["branch"], params["instance"])
    assert key in registry or key in foundations, f"Unprovisioned action: {key}"
    if key in registry:
        schema = SCHEMAS / registry[key]
        assert schema.is_file(), schema
        if params.get("xpath"):
            schema_root = ET.parse(schema)
            ns = {"xs": "http://www.w3.org/2001/XMLSchema"}
            tables = {
                "/root/" + node.attrib["name"]
                for node in schema_root.findall("./xs:element/xs:complexType/xs:sequence/xs:element", ns)
            }
            assert params["xpath"] in tables, f"Unknown table: {key} {params['xpath']}"
assert all(len(branch) <= 32 for branch, dataset in registry)
assert all((SCHEMAS / schema).is_file() for schema in registry.values())
foundation_contract = {
    ("CommonReferenceData", "CommonReferenceData"),
    ("Geographies", "Geographies"), ("UOM", "UOM"),
    ("Currencies", "Currencies"), ("Languages", "Languages")
}
for schema in registry.values():
    for reference in ET.parse(SCHEMAS / schema).findall(".//{urn:ebx-schemas:common_1.0}tableRef"):
        target = (reference.findtext("branch"), reference.findtext("container"))
        if target[0]:
            assert target in registry or target in foundation_contract, f"{schema}: unprovisioned FK target {target}"
content = ET.tostring(root.getroot(), encoding="unicode")
for forbidden in ("MCP", "Forge", "BankingCustomer", "OpenAIConfiguration", "media_mdm", "ebx-ps-fasttrack"):
    assert forbidden not in content, forbidden
assert "deleteHome(" not in source and "doDelete(" not in source
print(f"PASS: {len(items)} menu items, {len(registry)} registered datasets, local icons and schema targets")
