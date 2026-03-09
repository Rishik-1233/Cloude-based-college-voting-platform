#!/bin/bash
# ═══════════════════════════════════════════════════════
#  KLU Cloud Based College Voting Platform
#  Java + DSA Terminal Application
#  Run Script
# ═══════════════════════════════════════════════════════

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$SCRIPT_DIR/KLUVoting.jar"
SRC="$SCRIPT_DIR/src"
OUT="$SCRIPT_DIR/out"

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║    KLU Cloud Based College Voting Platform       ║"
echo "║    Java + DSA Terminal Application               ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

# Check Java
if ! command -v java &>/dev/null; then
    echo "ERROR: Java not found. Please install JDK 11+ and add to PATH."
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
echo "Java detected: version $JAVA_VER"

# If JAR exists, use it directly
if [ -f "$JAR" ]; then
    echo "Running from pre-compiled JAR..."
    java -jar "$JAR"
    exit $?
fi

# Otherwise compile from source
echo "JAR not found. Compiling from source..."

if command -v javac &>/dev/null; then
    echo "Using javac..."
    mkdir -p "$OUT"
    find "$SRC" -name "*.java" > /tmp/klu_sources.txt
    javac -d "$OUT" @/tmp/klu_sources.txt
    if [ $? -ne 0 ]; then
        echo "Compilation failed!"
        exit 1
    fi
else
    # Try the module approach (OpenJDK JRE with jdk.compiler module)
    echo "Trying module-based compilation..."
    mkdir -p "$OUT"
    find "$SRC" -name "*.java" > /tmp/klu_sources.txt
    java -m jdk.compiler/com.sun.tools.javac.Main -d "$OUT" @/tmp/klu_sources.txt
    if [ $? -ne 0 ]; then
        echo "Compilation failed! Please ensure you have a JDK (not just JRE) installed."
        exit 1
    fi
fi

echo "Compilation successful. Starting application..."
echo ""
java -cp "$OUT" klu.Main
