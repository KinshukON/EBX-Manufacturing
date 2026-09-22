package com.onebx.ebx.fasttrack.manufacturing.ui;

import com.orchestranetworks.schema.Path;
import com.orchestranetworks.ui.UIBeanEditor;
import com.orchestranetworks.ui.UIResponseContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Leaflet map UIBean for EBX Manufacturing Supplier Address records.
 *
 * <p>Embeds an interactive Leaflet map in the record form using the standard
 * {@link UIBeanEditor} API. Leaflet JS and CSS are loaded dynamically inside
 * {@code addJS} so that map initialisation is guaranteed to run <em>after</em>
 * Leaflet has fully downloaded (avoids blank-map race condition).</p>
 *
 * <p>The "Geocode Address" button calls the free Nominatim API to resolve
 * address fields → lat/lng. Coordinates are written back into the EBX form.</p>
 *
 * @author Kinshuk Dutta
 * @version 2.0.0
 * @since 6.2.3
 */
public class LeafletMapUIBean extends UIBeanEditor {

    /** Generates unique DOM IDs when multiple records are open concurrently. */
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    // ── Configurable via <osd:uiBean> child elements in the schema ───────────
    private String latitudeField      = "latitude";
    private String longitudeField     = "longitude";
    private String addressLine1Field  = "addressLine1";
    private String cityField          = "city";
    private String stateProvinceField = "stateProvince";
    private String postalCodeField    = "postalCode";
    private int    defaultZoom        = 14;
    private String mapHeight          = "380px";

    public void setLatitudeField(String v)      { this.latitudeField = v; }
    public void setLongitudeField(String v)     { this.longitudeField = v; }
    public void setAddressLine1Field(String v)  { this.addressLine1Field = v; }
    public void setCityField(String v)          { this.cityField = v; }
    public void setStateProvinceField(String v) { this.stateProvinceField = v; }
    public void setPostalCodeField(String v)    { this.postalCodeField = v; }
    public void setDefaultZoom(int v)           { this.defaultZoom = v; }
    public void setMapHeight(String v)          { this.mapHeight = v; }

    // ── UIBeanEditor API ─────────────────────────────────────────────────────

    @Override
    public void addForEdit(UIResponseContext pResponse) {
        renderMap(pResponse, true);
    }

    @Override
    public void addForDisplay(UIResponseContext pResponse) {
        renderMap(pResponse, false);
    }

    // ── Private rendering logic ──────────────────────────────────────────────

    private void renderMap(UIResponseContext pResponse, boolean editable) {

        // Read current field values from the record
        String lat    = getString(pResponse, latitudeField);
        String lng    = getString(pResponse, longitudeField);
        String addr1  = getString(pResponse, addressLine1Field);
        String city   = getString(pResponse, cityField);
        String state  = getString(pResponse, stateProvinceField);
        String postal = getString(pResponse, postalCodeField);

        boolean hasCoords = !lat.isEmpty() && !lng.isEmpty();

        // Unique DOM ids for this bean instance
        int    uid         = COUNTER.incrementAndGet();
        String mapDivId    = "ebxMap"        + uid;
        String markerId    = "ebxMarker"     + uid;
        String geocodeBtn  = "ebxGeocodeBtn" + uid;
        String statusId    = "ebxMapStatus"  + uid;
        String initFn      = "ebxMapInit"    + uid;

        // ── Map container HTML ───────────────────────────────────────────────
        pResponse.add("<div style='margin:10px 0;border:1px solid #d0d0d0;border-radius:6px;"
            + "overflow:hidden;font-family:inherit;box-shadow:0 1px 3px rgba(0,0,0,.1);'>");

        // Toolbar
        pResponse.add("  <div style='padding:6px 12px;background:#f7f7f7;border-bottom:1px solid #d0d0d0;"
            + "display:flex;align-items:center;gap:10px;flex-wrap:wrap;'>");
        pResponse.add("    <span style='font-size:13px;color:#444;font-weight:600;'>&#128205; Supplier Location Map</span>");

        if (editable) {
            pResponse.add("    <button id='" + geocodeBtn + "' "
                + "style='padding:3px 12px;background:#0069c0;color:#fff;border:none;border-radius:4px;"
                + "cursor:pointer;font-size:12px;font-weight:500;'>&#128269; Geocode Address</button>");
        }
        pResponse.add("    <span id='" + statusId + "' style='font-size:12px;color:#777;flex:1;'></span>");
        if (editable) {
            pResponse.add("    <span style='font-size:11px;color:#aaa;'>Click map or drag pin to set coordinates</span>");
        }
        pResponse.add("  </div>");

        // Map canvas
        pResponse.add("  <div id='" + mapDivId + "' style='height:" + mapHeight + ";width:100%;'></div>");
        pResponse.add("</div>");

        // ── All logic in addJS — Leaflet loaded dynamically with onload callback ──
        pResponse.addJS("(function() {");

        // Server-side values baked into JS
        pResponse.addJS("  var hasCoords = " + hasCoords + ";");
        pResponse.addJS("  var initLat   = " + (hasCoords ? lat   : "39.8283")  + ";");
        pResponse.addJS("  var initLng   = " + (hasCoords ? lng   : "-98.5795") + ";");
        pResponse.addJS("  var initZoom  = " + (hasCoords ? defaultZoom : 4)    + ";");
        pResponse.addJS("  var editable  = " + editable + ";");
        pResponse.addJS("  var defZoom   = " + defaultZoom + ";");
        pResponse.addJS("  var addr1     = '" + escJS(addr1)  + "';");
        pResponse.addJS("  var city      = '" + escJS(city)   + "';");
        pResponse.addJS("  var state     = '" + escJS(state)  + "';");
        pResponse.addJS("  var postal    = '" + escJS(postal) + "';");

        // ── Main init function (called after Leaflet loads) ──────────────────
        pResponse.addJS("  window." + initFn + " = function() {");

        pResponse.addJS("    function readDomField(hint) {");
        pResponse.addJS("      try {");
        pResponse.addJS("        var inputs = document.getElementsByTagName('input');");
        pResponse.addJS("        for (var i = 0; i < inputs.length; i++) {");
        pResponse.addJS("          var inp = inputs[i];");
        pResponse.addJS("          var nm = (inp.name || '') + (inp.id || '');");
        pResponse.addJS("          if (nm.indexOf(hint) >= 0 && inp.value && inp.value.trim()) {");
        pResponse.addJS("            var v = inp.value.trim();");
        pResponse.addJS("            if (!isNaN(parseFloat(v))) return v;");
        pResponse.addJS("          }");
        pResponse.addJS("        }");
        pResponse.addJS("      } catch(e) {}");
        pResponse.addJS("      return '';");
        pResponse.addJS("    }");
        pResponse.addJS("    var domLat = readDomField('latitude');");
        pResponse.addJS("    var domLng = readDomField('longitude');");
        pResponse.addJS("    if (domLat && domLng && !isNaN(parseFloat(domLat)) && !isNaN(parseFloat(domLng))) {");
        pResponse.addJS("      initLat   = parseFloat(domLat);");
        pResponse.addJS("      initLng   = parseFloat(domLng);");
        pResponse.addJS("      initZoom  = defZoom;");
        pResponse.addJS("      hasCoords = true;");
        pResponse.addJS("    }");
        pResponse.addJS("    var map = L.map('" + mapDivId + "').setView([initLat, initLng], initZoom);");
        pResponse.addJS("    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {");
        pResponse.addJS("      attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a>',");
        pResponse.addJS("      maxZoom: 19");
        pResponse.addJS("    }).addTo(map);");

        // Marker variable
        pResponse.addJS("    var " + markerId + " = null;");

        // ── EBX field updater ────────────────────────────────────────────────
        if (editable) {
            pResponse.addJS("    function setLatLng(lat, lng) {");
            pResponse.addJS("      if (typeof EBX_Form !== 'undefined' && EBX_Form.setNodeValue) {");
            pResponse.addJS("        EBX_Form.setNodeValue('" + bare(latitudeField)  + "', String(lat));");
            pResponse.addJS("        EBX_Form.setNodeValue('" + bare(longitudeField) + "', String(lng));");
            pResponse.addJS("      } else {");
            pResponse.addJS("        ['latitude','longitude'].forEach(function(fn, i) {");
            pResponse.addJS("          var v = i === 0 ? lat : lng;");
            pResponse.addJS("          document.querySelectorAll('input[name*=\"' + fn + '\"],input[id*=\"' + fn + '\"]')");
            pResponse.addJS("            .forEach(function(el) {");
            pResponse.addJS("              el.value = String(v);");
            pResponse.addJS("              el.dispatchEvent(new Event('input',  {bubbles:true}));");
            pResponse.addJS("              el.dispatchEvent(new Event('change', {bubbles:true}));");
            pResponse.addJS("            });");
            pResponse.addJS("        });");
            pResponse.addJS("      }");
            pResponse.addJS("    }");
        }

        // ── Marker helpers ───────────────────────────────────────────────────
        pResponse.addJS("    function onDragEnd(e) {");
        pResponse.addJS("      var p = e.target.getLatLng();");
        if (editable) {
            pResponse.addJS("      setLatLng(p.lat.toFixed(7), p.lng.toFixed(7));");
        }
        pResponse.addJS("      document.getElementById('" + statusId + "').textContent = '';");
        pResponse.addJS("    }");

        pResponse.addJS("    function placeMarker(latlng) {");
        pResponse.addJS("      if (" + markerId + ") {");
        pResponse.addJS("        " + markerId + ".setLatLng(latlng);");
        pResponse.addJS("      } else {");
        pResponse.addJS("        " + markerId + " = L.marker(latlng, {draggable: editable}).addTo(map);");
        pResponse.addJS("        " + markerId + ".on('dragend', onDragEnd);");
        pResponse.addJS("      }");
        pResponse.addJS("    }");

        // Place existing marker if coords present
        if (hasCoords) {
            pResponse.addJS("    placeMarker([initLat, initLng]);");
        }

        // Map click in edit mode
        if (editable) {
            pResponse.addJS("    map.on('click', function(e) {");
            pResponse.addJS("      placeMarker(e.latlng);");
            pResponse.addJS("      setLatLng(e.latlng.lat.toFixed(7), e.latlng.lng.toFixed(7));");
            pResponse.addJS("      document.getElementById('" + statusId + "').textContent = '';");
            pResponse.addJS("    });");
        }

        // ── Geocode button ───────────────────────────────────────────────────
        if (editable) {
            pResponse.addJS("    var btn    = document.getElementById('" + geocodeBtn + "');");
            pResponse.addJS("    var status = document.getElementById('" + statusId + "');");
            pResponse.addJS("    if (btn) btn.addEventListener('click', function() {");
            pResponse.addJS("      function readField(name) {");
            pResponse.addJS("        var el = document.querySelector('input[name*=\"' + name + '\"],input[id*=\"' + name + '\"]');");
            pResponse.addJS("        return el ? el.value.trim() : '';");
            pResponse.addJS("      }");
            pResponse.addJS("      var parts = [");
            pResponse.addJS("        readField('addressLine1') || addr1,");
            pResponse.addJS("        readField('city') || city,");
            pResponse.addJS("        readField('stateProvince') || state,");
            pResponse.addJS("        readField('postalCode') || postal");
            pResponse.addJS("      ].filter(function(v){ return v && v.trim(); });");
            pResponse.addJS("      var q = parts.join(', ');");
            pResponse.addJS("      if (!q) { status.style.color='#b71c1c'; status.textContent='\\u26a0 Fill in address fields first.'; return; }");
            pResponse.addJS("      btn.disabled = true; btn.textContent = 'Geocoding\\u2026';");
            pResponse.addJS("      status.style.color = '#777'; status.textContent = 'Searching\\u2026';");
            pResponse.addJS("      fetch('https://nominatim.openstreetmap.org/search?format=json&limit=1&q=' + encodeURIComponent(q),");
            pResponse.addJS("        { headers: { 'Accept-Language': 'en', 'User-Agent': 'EBX-ManufacturingModule/1.0' } })");
            pResponse.addJS("        .then(function(r) { return r.json(); })");
            pResponse.addJS("        .then(function(data) {");
            pResponse.addJS("          btn.disabled = false; btn.textContent = '\\u2315 Geocode Address';");
            pResponse.addJS("          if (data && data.length > 0) {");
            pResponse.addJS("            var rlat = parseFloat(data[0].lat).toFixed(7);");
            pResponse.addJS("            var rlng = parseFloat(data[0].lon).toFixed(7);");
            pResponse.addJS("            setLatLng(rlat, rlng);");
            pResponse.addJS("            var ll = [parseFloat(rlat), parseFloat(rlng)];");
            pResponse.addJS("            placeMarker(ll); map.setView(ll, defZoom);");
            pResponse.addJS("            var label = data[0].display_name;");
            pResponse.addJS("            status.style.color = '#2e7d32';");
            pResponse.addJS("            status.textContent = '\\u2713 ' + (label.length > 70 ? label.substring(0,70)+'\\u2026' : label);");
            pResponse.addJS("          } else {");
            pResponse.addJS("            status.style.color = '#b71c1c';");
            pResponse.addJS("            status.textContent = '\\u26a0 Address not found \\u2014 add more detail.';");
            pResponse.addJS("          }");
            pResponse.addJS("        })");
            pResponse.addJS("        .catch(function(err) {");
            pResponse.addJS("          btn.disabled = false; btn.textContent = '\\u2315 Geocode Address';");
            pResponse.addJS("          status.style.color = '#b71c1c';");
            pResponse.addJS("          status.textContent = '\\u26a0 Geocoding failed \\u2014 check network.';");
            pResponse.addJS("        });");
            pResponse.addJS("    });");
        }

        // ── Full-width expansion ─────────────────────────────────────────────
        pResponse.addJS("    (function expandWidth() {");
        pResponse.addJS("      var mapDiv = document.getElementById('" + mapDivId + "');");
        pResponse.addJS("      if (!mapDiv) return;");
        pResponse.addJS("      var el = mapDiv.parentElement;");
        pResponse.addJS("      while (el && el.tagName !== 'TD') el = el.parentElement;");
        pResponse.addJS("      if (el) {");
        pResponse.addJS("        el.style.width = '100%'; el.colSpan = 2;");
        pResponse.addJS("        var labelTd = el.previousElementSibling;");
        pResponse.addJS("        if (labelTd && labelTd.tagName === 'TD') labelTd.style.display = 'none';");
        pResponse.addJS("        setTimeout(function() { map.invalidateSize(); }, 100);");
        pResponse.addJS("      }");
        pResponse.addJS("    })();");

        pResponse.addJS("  };"); // end initFn

        // ── Dynamic Leaflet CSS + JS loader ──────────────────────────────────
        pResponse.addJS("  if (typeof L !== 'undefined') {");
        pResponse.addJS("    window." + initFn + "();");
        pResponse.addJS("  } else {");
        pResponse.addJS("    var cssLink = document.createElement('link');");
        pResponse.addJS("    cssLink.rel  = 'stylesheet';");
        pResponse.addJS("    cssLink.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';");
        pResponse.addJS("    document.head.appendChild(cssLink);");
        pResponse.addJS("    var script   = document.createElement('script');");
        pResponse.addJS("    script.src   = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';");
        pResponse.addJS("    script.onload = window." + initFn + ";");
        pResponse.addJS("    script.onerror = function() {");
        pResponse.addJS("      document.getElementById('" + statusId + "').textContent =");
        pResponse.addJS("        '\\u26a0 Map unavailable \\u2014 Leaflet could not load.';");
        pResponse.addJS("    };");
        pResponse.addJS("    document.head.appendChild(script);");
        pResponse.addJS("  }");

        pResponse.addJS("})();");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String getString(UIResponseContext ctx, String fieldPath) {
        try {
            Object val = ctx.getValue(Path.parse(fieldPath));
            return val != null ? val.toString().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String bare(String path) {
        return path.replaceAll("^\\./", "").replaceAll("^/", "");
    }

    private static String escJS(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
    }
}
