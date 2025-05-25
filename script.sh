set -e

mvn compile -am spotless:check modernizer:modernizer spotbugs:check pmd:check pmd:cpd-check

mvn clean install

wait
