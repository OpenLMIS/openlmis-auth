# Demo Data for OpenLMIS Authentication Service
This folder holds demo data for the auth service. The demo data is used by developers, QA staff,
and is automatically loaded into some environments for demo and testing purposes. It is not for use
in production environments.

Each .json file contains demo data that corresponds to one database table.

## Users (auth.auth_users.json)
There are 13 user accounts:

1. administrator (password=password)
2. devadmin (password=password)
3. srmanager1 (password=password)
4. srmanager2 (password=password)
5. srmanager3 (password=password)
6. srmanager4 (password=password)
7. smanager1 (password=password)
8. smanager2 (password=password)
9. smanager3 (password=password)
10. smanager4 (password=password)
11. dsrmanager (password=password)
12. psupervisor (password=password)
13. wclerk (password=password)

The [Reference Data service's demo data](https://github.com/OpenLMIS/openlmis-referencedata/tree/master/demo-data)
connects these users to specific rights at facilities and programs.

These credentials are public knowledge. They are not secure. Do not use any of these accounts or
passwords in any production OpenLMIS environment.
