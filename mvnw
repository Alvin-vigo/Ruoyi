#!/usr/bin/env bash
# ============================================================
#  项目内置 Maven 启动脚本（Git Bash / Linux / macOS）
#  用法：./mvnw clean install -DskipTests
#  说明：使用 tools/ 下随项目携带的 Maven，无需单独安装。
#
#  注：部分精简 Git Bash 缺少 uname/dirname 等 coreutils 命令，
#      标准的 maven/bin/mvn 脚本会执行失败，因此这里直接以
#      java + classworlds 启动器的方式调用 Maven。
# ============================================================
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAVEN_HOME="$PROJECT_DIR/tools/apache-maven-3.9.6"
CW_JAR="$MAVEN_HOME/boot/plexus-classworlds-2.7.0.jar"
M2_CONF="$MAVEN_HOME/bin/m2.conf"

if [ ! -f "$CW_JAR" ]; then
  echo "[ERROR] 未找到项目内置 Maven: $CW_JAR" >&2
  echo "        请确认 tools/apache-maven-3.9.6 目录随项目一起拷贝。" >&2
  exit 1
fi

# 定位 java 可执行文件
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java.exe" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java.exe"
else
  JAVA_BIN="$(command -v java || true)"
fi

if [ -z "$JAVA_BIN" ]; then
  echo "[ERROR] 未找到 java，请设置 JAVA_HOME 或把 JDK 加入 PATH。" >&2
  exit 1
fi

export MAVEN_OPTS="$MAVEN_OPTS -Dfile.encoding=UTF-8"

# Git Bash（MSYS）下把 /c/... 形式的路径转成 Windows 原生写法，
# 避免 JVM 参数被 MSYS 路径转换规则改写导致 classpath 失效。
to_win_path() {
  if command -v cygpath >/dev/null 2>&1; then
    cygpath -w "$1"
  else
    case "$1" in
      /?/*) printf '%s\n' "$(printf '%s' "${1:1:1}"):${1:2}" | tr '/' '\\' ;;
      *)    printf '%s\n' "$1" ;;
    esac
  fi
}

JAVA_BIN_W="$(to_win_path "$JAVA_BIN")"
MAVEN_HOME_W="$(to_win_path "$MAVEN_HOME")"
PROJECT_DIR_W="$(to_win_path "$PROJECT_DIR")"
M2_CONF_W="$(to_win_path "$M2_CONF")"
CW_JAR_W="$(to_win_path "$CW_JAR")"

exec "$JAVA_BIN" \
  -Dmaven.home="$MAVEN_HOME_W" \
  -Dmaven.multiModuleProjectDirectory="$PROJECT_DIR_W" \
  -Dclassworlds.conf="$M2_CONF_W" \
  -classpath "$CW_JAR_W" \
  -Dfile.encoding=UTF-8 \
  org.codehaus.plexus.classworlds.launcher.Launcher \
  -f "$PROJECT_DIR_W/pom.xml" "$@"
