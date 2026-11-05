#!/usr/bin/env python3
"""Modelo de estados hipotéticos; no es un emulador de circuitos ni una prueba USB física."""
from __future__ import annotations
import argparse
import datetime as dt
import json
from dataclasses import asdict, dataclass
from pathlib import Path


@dataclass(frozen=True)
class Case:
    name: str
    power_reaches_phone: bool
    usb_data_path: bool
    phone_otg_and_charge: bool | None
    drive_inserted: bool = True
    media_writable: bool = True
    description: str = ""


def evaluate(case: Case) -> dict:
    charge = bool(case.power_reaches_phone)
    files = bool(case.usb_data_path and case.drive_inserted)
    if not charge or not files:
        combined = "NO"
    elif case.phone_otg_and_charge is None:
        combined = "PENDIENTE_VALIDACION_FISICA"
    else:
        combined = "SI_EN_EL_MODELO" if case.phone_otg_and_charge else "NO"
    access = files and (not charge or case.phone_otg_and_charge is True)
    return {
        "escenario": case.name,
        "carga_teorica": charge,
        "ruta_datos_usb_modelada": files,
        "acceso_archivos_teorico": access if case.phone_otg_and_charge is not None or not charge else "INCIERTO",
        "lectura_y_escritura_si_montado": bool(case.media_writable and files),
        "simultaneidad": combined,
        "explicacion": case.description,
        "condicion_importante": "No verifica circuito interno PB855, negociación PD ni compatibilidad física del teléfono",
    }


CASES = [
    Case("memoria_directa_en_telefono", False, True, None, description="El teléfono funciona como host OTG; no recibe carga."),
    Case("memoria_en_powerbank_sin_puente_datos", True, False, None, description="Escenario supuesto: power bank solo entrega alimentación, sin rutas D+/D-/USB-C de datos."),
    Case("hub_pd_otg_telefono_compatible", True, True, True, description="Escenario positivo hipotético: hub soporta host USB y PD passthrough, teléfono también."),
    Case("hub_pd_otg_telefono_no_compatible", True, True, False, description="Escenario negativo hipotético: el teléfono no sostiene host OTG mientras carga."),
    Case("hub_pd_otg_compatibilidad_desconocida", True, True, None, description="Estado real actual: falta ensayo físico de carga + OTG."),
    Case("hub_pd_otg_sin_memoria", True, True, True, drive_inserted=False, description="La memoria fue extraída o se perdió la conexión."),
    Case("hub_pd_otg_memoria_solo_lectura", True, True, True, media_writable=False, description="La memoria o el DocumentsProvider no admite escritura."),
]


def run() -> dict:
    results = [evaluate(c) for c in CASES]
    assert results[0]["carga_teorica"] is False and results[0]["ruta_datos_usb_modelada"] is True
    assert results[1]["simultaneidad"] == "NO"
    assert results[2]["simultaneidad"] == "SI_EN_EL_MODELO"
    assert results[3]["simultaneidad"] == "NO"
    assert results[4]["simultaneidad"] == "PENDIENTE_VALIDACION_FISICA"
    assert results[5]["acceso_archivos_teorico"] is False
    assert results[6]["lectura_y_escritura_si_montado"] is False
    return {
        "sistema": "GENAI USB POWER BRIDGE",
        "version": "0.1.0-sandbox",
        "fecha_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
        "tipo": "SIMULACION_LOGICA_NO_HARDWARE",
        "resultado_tests": "7/7 APROBADOS",
        "escenarios": results,
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", type=Path, default=Path(__file__).with_name("resultado_simulacion.json"))
    args = parser.parse_args()
    summary = run()
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("GENAI USB POWER BRIDGE | SANDBOX")
    print("Resultado: 7/7 escenarios validados en el modelo lógico")
    for item in summary["escenarios"]:
        print(f"- {item['escenario']}: simultaneidad={item['simultaneidad']}")
    print("Reporte:", args.out)
