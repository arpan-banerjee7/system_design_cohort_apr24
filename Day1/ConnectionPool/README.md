# system_design_cohort_apr24
## Basic implementation of Connection pool including min,max connections and idle timeouts
1. docker pull mysql
2. docker run --name mysql-container -e MYSQL_ROOT_PASSWORD=myrootpassword -e MYSQL_DATABASE=test -e MYSQL_USER=test -e MYSQL_PASSWORD=test -p 3306:3306 -d mysql