#!/usr/bin/env python3
"""Ensayos POSIX temporales, independientes de Android SAF y del USB físico."""
from __future__ import annotations
import hashlib
import json
import shutil
import tempfile
from pathlib import Path


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify_copy(source: Path, dest: Path) -> None:
    shutil.copyfile(source, dest)
    if sha(source) != sha(dest):
        dest.unlink(missing_ok=True)
        raise IOError("integridad incorrecta")


def safe_delete_with_backup(source: Path, backup: Path, *, injected_error: bool = False) -> None:
    if injected_error:
        raise IOError("escritura del respaldo rechazada (simulada)")
    verify_copy(source, backup)
    source.unlink()  # Solo después de verificar el respaldo.


def run() -> dict:
    results: list[dict] = []
    with tempfile.TemporaryDirectory(prefix="genai_usb_bridge_") as tmp:
        root = Path(tmp)
        original = root / "original.txt"
        original.write_text("GENAI: archivo original intocable\n", encoding="utf-8")
        digest = sha(original)

        # 1. Leer sin cambiar el original.
        assert original.read_text(encoding="utf-8") == "GENAI: archivo original intocable\n"
        results.append({"prueba": "leer", "resultado": "PASS"})

        # 2. Editar creando una copia, preservando hash original.
        edited = root / "original_EDITADO.txt"
        edited.write_text("GENAI: texto editado en COPIA\n", encoding="utf-8")
        assert sha(original) == digest and sha(edited) != digest
        results.append({"prueba": "editar_sin_sobrescribir", "resultado": "PASS"})

        # 3. Copiar archivo verificando hash.
        copy = root / "original_COPIA.txt"
        verify_copy(original, copy)
        assert sha(copy) == digest and sha(original) == digest
        results.append({"prueba": "copiar_hash_sha256", "resultado": "PASS"})

        # 4. Si respaldo falla, el original debe mantenerse.
        try:
            safe_delete_with_backup(original, root / "RESPALDO_FALLIDO.txt", injected_error=True)
        except IOError:
            pass
        else:
            raise AssertionError("se esperaba falla simulada")
        assert original.exists() and sha(original) == digest
        results.append({"prueba": "fallo_respaldo_conserva_original", "resultado": "PASS"})

        # 5. Eliminar *archivo temporal de prueba* tras respaldo verificado.
        trial = root / "solo_prueba_borrado.txt"
        trial.write_text("archivo de prueba; sin datos del usuario", encoding="utf-8")
        digest_trial = sha(trial)
        backup = root / "solo_prueba_RESPALDO.txt"
        safe_delete_with_backup(trial, backup)
        assert not trial.exists() and backup.exists() and sha(backup) == digest_trial
        assert original.exists() and sha(original) == digest
        results.append({"prueba": "borrar_temporal_solo_tras_respaldo", "resultado": "PASS"})

        # 6. Una memoria desconectada o inexistente no debe crear archivos.
        try:
            verify_copy(root / "memoria_desconectada.txt", root / "resultado_invalido.txt")
        except FileNotFoundError:
            pass
        else:
            raise AssertionError("se esperaba error de ausencia")
        assert not (root / "resultado_invalido.txt").exists()
        results.append({"prueba": "almacenamiento_ausente_falla_seguro", "resultado": "PASS"})
    return {"categoria": "ENSAYOS_FILE_SYSTEM_TEMPORAL_NO_ANDROID", "tests": results, "passed": len(results)}


if __name__ == "__main__":
    summary = run()
    dest = Path(__file__).with_name("resultado_archivos.json")
    dest.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"OPERACIONES DE ARCHIVOS EN SANDBOX: {summary['passed']}/{summary['passed']} PASS")
    print("Archivos reales del usuario: SIN ACCESO. Android SAF: NO PROBADO.")
