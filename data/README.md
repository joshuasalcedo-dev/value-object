# data/: where the library's reference data comes from

The files in `src/main/resources/io/joshuasalcedo/valueobject/data/` are **generated**. Don't edit them by hand:
change the extractor or the pinned source, run it, and commit both.

| Output | Extractor | Source | Licence |
|---|---|---|---|
| `tzdata.json` | `Tzdata.java` | IANA time zone database, release pinned in `sources.properties` | public domain |
| `countries.json` | `Countries.java` | Wikidata (snapshot in `raw/wikidata-countries.csv`) + IANA `iso3166.tab` / `zone.tab` | CC0 / public domain |

Run from the repo root (Java 25, no dependencies):

```sh
java data/Tzdata.java              # write tzdata.json
java data/Tzdata.java --latest     # move the pin to IANA's newest release, then write
java data/Countries.java           # write countries.json from the Wikidata snapshot (offline)
java data/Countries.java --refresh # ask Wikidata again, rewrite the snapshot, then write
java data/Tzdata.java --check      # CI: fail when the output is not what the sources give
java data/Countries.java --check
```

- **Pinned:** a run gives the same bytes on any machine. Downloads are checked against their `sha256` and kept in
  `data/cache/` (not in git).
- **Wikidata changes daily**, so its answer is kept in `raw/wikidata-countries.csv` (one `item,alpha2,field,value`
  per line). A `--refresh` is a readable diff of that file.
- **Countries are IANA's 249 ISO 3166-1 codes**, the same as `Country`. When one code is on several Wikidata items,
  `sources.properties` names the right one (`wikidata.item.AQ=Q51`).
- **To add a country field:** add a line to `Countries.FIELDS` (the SPARQL for `?value`), run `--refresh`, and add
  it in `Countries.country`.

`tzdata.json` keeps the shape of the earlier hand-made file (`countries`, `zones` from `zone.tab`) and adds each
zone's current `utc_offset`, `rules` and `format`, `zone1970`, `all_zones` and `links` (old names → their zone).
