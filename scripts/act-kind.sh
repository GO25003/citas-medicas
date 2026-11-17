#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

for tool in act docker kind kubectl flock; do
  command -v "$tool" >/dev/null || { echo "Falta el comando requerido: $tool" >&2; exit 1; }
done

test -f .act.secrets || {
  echo "Falta .act.secrets. Consulta la sección CI/CD con ACT del README.md." >&2
  exit 1
}

context="$(kubectl config current-context)"
test "$context" = kind-citas-medicas || {
  echo "Contexto actual: $context; se esperaba kind-citas-medicas." >&2
  exit 1
}
kind get clusters | grep -Fx citas-medicas >/dev/null || {
  echo "No se encontró el clúster Kind citas-medicas." >&2
  exit 1
}

docker_endpoint="$(docker context inspect "$(docker context show)" --format '{{.Endpoints.docker.Host}}')"
case "$docker_endpoint" in
  unix://*) docker_socket="${docker_endpoint#unix://}" ;;
  *) echo "Se requiere un daemon Docker local por socket Unix; encontrado: $docker_endpoint" >&2; exit 1 ;;
esac
test -S "$docker_socket" || { echo "No existe el socket Docker: $docker_socket" >&2; exit 1; }
docker info >/dev/null

lock_file="${XDG_RUNTIME_DIR:-${TMPDIR:-/tmp}}/citas-medicas-act-deploy.lock"
exec 9>"$lock_file"
flock -n 9 || { echo "Otro despliegue ACT está ejecutándose." >&2; exit 1; }

temporary_dir="$(mktemp -d "${TMPDIR:-/tmp}/citas-medicas-act.XXXXXX")"
chmod 700 "$temporary_dir"
trap 'rm -rf "$temporary_dir"' EXIT
kubeconfig_file="$temporary_dir/kubeconfig"
umask 077
kubectl config view --raw --flatten --minify > "$kubeconfig_file"
test "$(kubectl --kubeconfig "$kubeconfig_file" config current-context)" = kind-citas-medicas

docker_bin="$(command -v docker)"
kind_bin="$(command -v kind)"
kubectl_bin="$(command -v kubectl)"
container_options="--security-opt label=disable --volume $docker_bin:/usr/local/bin/docker:ro --volume $kind_bin:/usr/local/bin/kind:ro --volume $kubectl_bin:/usr/local/bin/kubectl:ro --volume $kubeconfig_file:/run/act-kubeconfig:ro"

act workflow_dispatch \
  --workflows .github/workflows/ci.yml \
  --input deploy_local=true \
  --platform ubuntu-latest=catthehacker/ubuntu:act-latest \
  --env-file /dev/null \
  --var-file /dev/null \
  --secret-file .act.secrets \
  --env ACT=true \
  --env DOCKER_HOST=unix:///var/run/docker.sock \
  --env KUBECONFIG=/run/act-kubeconfig \
  --container-daemon-socket "unix://$docker_socket" \
  --network host \
  --container-options "$container_options"
