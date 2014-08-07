#!/bin/sh
# ----------------------------------------------------------------------------
# Kickoff script for the Amazon API based SNS Publisher
#   publish a message to the SNS topic each second
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Required ENV vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir
# ----------------------------------------------------------------------------

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true
           #
           # Look for the Apple JDKs first to preserve the existing behaviour, and then look
           # for the new JDKs provided by Oracle.
           #
           if [[ -z "$JAVA_HOME" && -L /System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK ]] ; then
             #
             # Apple JDKs
             #
             export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home
           fi

           if [[ -z "$JAVA_HOME" && -L /System/Library/Java/JavaVirtualMachines/CurrentJDK ]] ; then
             #
             # Apple JDKs
             #
             export JAVA_HOME=/System/Library/Java/JavaVirtualMachines/CurrentJDK/Contents/Home
           fi

           if [[ -z "$JAVA_HOME" && -L "/Library/Java/JavaVirtualMachines/CurrentJDK" ]] ; then
             #
             # Oracle JDKs
             #
             export JAVA_HOME=/Library/Java/JavaVirtualMachines/CurrentJDK/Contents/Home
           fi
           ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# For Migwn, ensure paths are in UNIX format before anything is touched
if $mingw ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME="`(cd "$JAVA_HOME"; pwd)`"
  # TODO classpath?
fi

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="`which javac`"
  if [ -n "$javaExecutable" -a ! "`expr \"$javaExecutable\" : '\([^ ]*\)'`" = "no" ]; then
    # readlink(1) is not available as standard on Solaris 10.
    readLink=`which readlink`
    if [ ! `expr "$readLink" : '\([^ ]*\)'` = "no" ]; then
      javaExecutable="`readlink -f \"$javaExecutable\"`"
      javaHome="`dirname \"$javaExecutable\"`"
      javaHome=`expr "$javaHome" : '\(.*\)/bin'`
      JAVA_HOME="$javaHome"
      export JAVA_HOME
    fi
  fi
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="`which java`"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

CLASSWORLDS_LAUNCHER=org.codehaus.plexus.classworlds.launcher.Launcher

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# Build the classpath
EXEDIR=`dirname $0`
BASEEXEDIR=`dirname $EXEDIR`

APP_CLSPATH+=${BASEEXEDIR}/lib/classes
for jarFile in $( ls ${BASEEXEDIR}/lib/*.jar )
  do
    APP_CLSPATH+=":$jarFile"
  done
echo $APP_CLSPATH
exec "$JAVACMD" \
  -classpath ${APP_CLSPATH} \
  com.charter.aesd.aws.snsclient.demo.RawSNS
