1. STUDENT (Public Registration)

Create directly in the browser at http://localhost/register with any valid email and strong password (8–72 characters, including uppercase, lowercase, a number, and a special character).

2. ADMIN (One-Time Interactive Bootstrap CLI)

Run inside your project root (d:\MAFIOSO\MAFIOSO)—you will be prompted for Full Name, Email, and a hidden Password:

powershell
docker compose exec -it backend java -cp "/usr/local/tomcat/webapps/ROOT/WEB-INF/classes:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib/*" com.mafioso.bootstrap.AdminBootstrapCli
3. CONSULTANT (Staff Provisioning CLI)

Consultant accounts cannot register publicly; provision one securely via:

powershell
docker compose exec -it backend java -cp "/usr/local/tomcat/webapps/ROOT/WEB-INF/classes:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib/*" com.mafioso.bootstrap.ConsultantProvisionCli

Once created, sign in with each account's email and chosen password at http://localhost/login (see AUTHENTICATION_AND_SECURITY.md for non-interactive secret options).