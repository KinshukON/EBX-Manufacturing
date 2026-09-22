package com.onebx.ebx.fasttrack.manufacturing;

/** Deployed module identifiers used by schema URNs and service registration. */
public final class ModuleNames {
    public static final String MANUFACTURING = "ebx-manufacturing";
    public static final String REFERENCE_DATA = "ebx-reference-data";

    private ModuleNames() { }

    public static String schema(final String module, final String resource) {
        return "urn:ebx:module:" + module + ":/WEB-INF/ebx/schemas/" + resource;
    }
}
