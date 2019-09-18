package libcore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import libcore.util.CountryTimeZones;

public final class CountryZonesFinder {
    private final List<CountryTimeZones> countryTimeZonesList;

    CountryZonesFinder(List<CountryTimeZones> countryTimeZonesList2) {
        this.countryTimeZonesList = new ArrayList(countryTimeZonesList2);
    }

    public static CountryZonesFinder createForTests(List<CountryTimeZones> countryTimeZonesList2) {
        return new CountryZonesFinder(countryTimeZonesList2);
    }

    public List<String> lookupAllCountryIsoCodes() {
        List<String> isoCodes = new ArrayList<>(this.countryTimeZonesList.size());
        for (CountryTimeZones countryTimeZones : this.countryTimeZonesList) {
            isoCodes.add(countryTimeZones.getCountryIso());
        }
        return Collections.unmodifiableList(isoCodes);
    }

    public List<CountryTimeZones> lookupCountryTimeZonesForZoneId(String zoneId) {
        List<CountryTimeZones> matches = new ArrayList<>(2);
        for (CountryTimeZones countryTimeZones : this.countryTimeZonesList) {
            if (CountryTimeZones.TimeZoneMapping.containsTimeZoneId(countryTimeZones.getTimeZoneMappings(), zoneId)) {
                matches.add(countryTimeZones);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    public CountryTimeZones lookupCountryTimeZones(String countryIso) {
        String normalizedCountryIso = TimeZoneFinder.normalizeCountryIso(countryIso);
        for (CountryTimeZones countryTimeZones : this.countryTimeZonesList) {
            if (countryTimeZones.getCountryIso().equals(normalizedCountryIso)) {
                return countryTimeZones;
            }
        }
        return null;
    }
}
