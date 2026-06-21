import re
from pathlib import Path

source = Path(__file__).resolve().parents[3] / (
    "other_contributions/OTP_IsInTransport_PoC/app/src/main/java/com/otpiitpoc/LisbonTransportStations.kt"
)
target = Path(__file__).resolve().parents[1] / "modules/host/src/main/resources/db/migration/V2__seed_stations.sql"

text = source.read_text(encoding="utf-8")
rows: list[str] = []
for line in text.splitlines():
    stripped = line.strip()
    if not stripped.startswith("TransportStation("):
        continue
    match = re.search(
        r'TransportStation\("([^"]+)", "([^"]+)", ([\d.-]+), ([\d.-]+)\)',
        stripped,
    )
    if not match:
        continue
    station_id, name, lat, lng = match.groups()
    transport_type = "METRO" if station_id.endswith("_metro") else "TRAIN"
    name_escaped = name.replace("'", "''")
    rows.append(f"    ('{station_id}', '{name_escaped}', {lat}, {lng}, '{transport_type}')")

lines = [
    "-- Seed: estações Metro e Comboio (Lisboa)",
    f"-- Fonte: {source.as_posix()}",
    f"-- Total: {len(rows)} estações",
    "",
    "INSERT INTO stations (id, name, latitude, longitude, transport_type)",
    "VALUES",
    ",\n".join(rows),
    "ON CONFLICT (id) DO NOTHING;",
    "",
]
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text("\n".join(lines), encoding="utf-8")
print(f"Wrote {len(rows)} stations to {target}")
