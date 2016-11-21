# Demo Data for OpenLMIS Authentication Service
This folder holds demo data for the auth service. The demo data is used by developers, QA staff,
and is automatically loaded into some environments for demo and testing purposes. It is not for use
in production environments.

Each .json file contains demo data that corresponds to one database table.

## Users (auth_users)
There are 2 user accounts:

1. administrator (password=password)
2. devadmin (password=password)

The [Reference Data service demo data](https://github.com/OpenLMIS/openlmis-referencedata/tree/master/demo-data)
connects these users to specific rights at facilities and programs.
