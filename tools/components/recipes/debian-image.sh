#!/usr/bin/env bash
set -euo pipefail
. tools/linuxfs/debian-image.env
image=public.ecr.aws/docker/library/debian:bullseye
upstream=Droid-Deck/DroidDeck
repository="${GITHUB_REPOSITORY:-$upstream}"
archive_dir="$(mktemp -d "${TMPDIR:-/tmp}/droiddeck-debian.XXXXXXXX")"
trap 'rm -rf "$archive_dir"' EXIT
archive="$archive_dir/$DEBIAN_IMAGE_ASSET"

# Fork releases generally do not contain the upstream's pinned CI image.
# Use the fork's release first, then the canonical upstream release. In both
# cases check the SHA256 pinned in tools/linuxfs/debian-image.env before loading.
fetch_release() {
  local repo="$1"
  rm -f "$archive"
  gh release download "$DEBIAN_IMAGE_TAG" -R "$repo" -p "$DEBIAN_IMAGE_ASSET" -O "$archive"
}

source_repo=""
if fetch_release "$repository"; then
  source_repo="$repository"
elif [[ "$repository" != "$upstream" ]] && fetch_release "$upstream"; then
  source_repo="$upstream"
fi

if [[ -n "$source_repo" ]]; then
  echo "Using pinned Debian image from $source_repo"
  if [[ -n "$DEBIAN_IMAGE_SHA256" ]]; then
    echo "$DEBIAN_IMAGE_SHA256  $archive" | sha256sum -c - || {
      echo "::error::The Debian image release asset did not match its pinned SHA256"
      exit 1
    }
  fi
  zstd -dc "$archive" | docker load
else
  echo "::warning::the pinned Debian image could not be fetched from $repository or $upstream; pulling from the registry instead"
  for attempt in 1 2 3 4 5; do
    docker pull "$image" && break
    [[ "$attempt" -lt 5 ]] || { echo "::error::could not pull $image after $attempt attempts"; exit 1; }
    echo "pull attempt $attempt failed (registry busy?); retrying in $((attempt * 30)) s"
    sleep $((attempt * 30))
  done
fi
docker image ls "$image"
