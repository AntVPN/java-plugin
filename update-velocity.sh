#!/bin/bash
# update-velocity.sh — Checks if the local velocity.jar is the latest build
# from PaperMC and downloads the new one if it isn't.
#
# API: https://fill.papermc.io/
#
# Usage:
#   ./update-velocity.sh [options]
#
# Options:
#   -d, --dir <path>       Directory containing velocity.jar (default: current dir)
#   -v, --version <ver>    Target Velocity version (default: latest supported)
#   -n, --dry-run          Only check, don't download
#   -h, --help             Show this help message
#
# Environment variables:
#   VELOCITY_JAR_DIR       Same as --dir
#   VELOCITY_VERSION       Same as --version

set -euo pipefail

API_BASE="https://fill.papermc.io/v3"
PROJECT="velocity"
JAR_NAME="velocity.jar"

# Defaults
jar_dir="${VELOCITY_JAR_DIR:-.}"
target_version="${VELOCITY_VERSION:-}"
dry_run=false

# ── Colors ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()  { printf "${CYAN}[INFO]${NC}  %s\n" "$*"; }
ok()    { printf "${GREEN}[ OK ]${NC}  %s\n" "$*"; }
warn()  { printf "${YELLOW}[WARN]${NC}  %s\n" "$*"; }
err()   { printf "${RED}[ERR]${NC}   %s\n" "$*" >&2; }

usage() {
    sed -n '/^# Usage:/,/^$/p' "$0" | sed 's/^# \?//'
    exit 0
}

# ── Argument parsing ─────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
    case "$1" in
        -d|--dir)       jar_dir="$2"; shift 2 ;;
        -v|--version)   target_version="$2"; shift 2 ;;
        -n|--dry-run)   dry_run=true; shift ;;
        -h|--help)      usage ;;
        *)              err "Unknown option: $1"; usage ;;
    esac
done

jar_path="${jar_dir}/${JAR_NAME}"

# ── Dependency check ─────────────────────────────────────────────────────────
for cmd in curl jq sha256sum; do
    if ! command -v "$cmd" &>/dev/null; then
        err "'$cmd' is required but not installed."
        exit 1
    fi
done

# ── Resolve latest supported version if not specified ────────────────────────
if [[ -z "$target_version" ]]; then
    info "Fetching available versions..."
    versions_json=$(curl -sSf "${API_BASE}/projects/${PROJECT}/versions")

    target_version=$(printf '%s' "$versions_json" | jq -r '
        [.versions[] | select(.version.support.status == "SUPPORTED") | .version.id] | first
    ')

    if [[ -z "$target_version" || "$target_version" == "null" ]]; then
        err "Could not determine the latest supported version."
        exit 1
    fi
fi
info "Target version: ${target_version}"

# ── Fetch latest build metadata ──────────────────────────────────────────────
info "Fetching latest build for ${target_version}..."
build_json=$(curl -sSf "${API_BASE}/projects/${PROJECT}/versions/${target_version}/builds/latest")

build_number=$(printf '%s' "$build_json" | jq -r '.id')
remote_sha256=$(printf '%s' "$build_json" | jq -r '.downloads["server:default"].checksums.sha256')
download_name=$(printf '%s' "$build_json" | jq -r '.downloads["server:default"].name')
download_url=$(printf '%s' "$build_json" | jq -r '.downloads["server:default"].url')

if [[ -z "$build_number" || "$build_number" == "null" ]]; then
    err "Could not fetch latest build information."
    exit 1
fi
info "Latest build: #${build_number} (${download_name})"

# ── Compare local jar ────────────────────────────────────────────────────────
if [[ -f "$jar_path" ]]; then
    local_sha256=$(sha256sum "$jar_path" | awk '{print $1}')
    if [[ "$local_sha256" == "$remote_sha256" ]]; then
        ok "velocity.jar is already up to date (build #${build_number})."
        exit 0
    fi
    warn "Local jar is outdated."
    info "  Local  SHA256: ${local_sha256}"
    info "  Remote SHA256: ${remote_sha256}"
else
    warn "No velocity.jar found at ${jar_path}"
fi

# ── Dry-run exit ─────────────────────────────────────────────────────────────
if $dry_run; then
    info "Dry-run mode — skipping download."
    exit 0
fi

# ── Download ─────────────────────────────────────────────────────────────────
info "Downloading ${download_name}..."
tmp_file="${jar_path}.tmp"

if ! curl -sSf -o "$tmp_file" "$download_url"; then
    err "Download failed."
    rm -f "$tmp_file"
    exit 1
fi

# ── Verify checksum ──────────────────────────────────────────────────────────
dl_sha256=$(sha256sum "$tmp_file" | awk '{print $1}')
if [[ "$dl_sha256" != "$remote_sha256" ]]; then
    err "Checksum mismatch after download!"
    err "  Expected: ${remote_sha256}"
    err "  Got:      ${dl_sha256}"
    rm -f "$tmp_file"
    exit 1
fi

# ── Replace jar ──────────────────────────────────────────────────────────────
mv -f "$tmp_file" "$jar_path"
ok "Updated velocity.jar → build #${build_number} (${download_name})"
ok "SHA256: ${remote_sha256}"
