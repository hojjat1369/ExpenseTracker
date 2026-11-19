
# Expense Tracker
> A module to track your expenses

This repository contains the Expense Tracker source code. Expense Tracker is a Java module developed to ease the process of tracking daily expenses. It's powerful, actively maintained and easy to use.”

TODO:

* Add more unit tests
* Add integration tests
* Dokerise the project

## Getting started

This Expense Tracker provides simple APIs to work with different types of expense. You can:

```shell
* Signup/Login/Get data about your user.
* Ceate/Update/Delete/Get/Search differnt tpyes of Categories for yourself.
* Create/Update/Delete/Get/Search you expenses based of different categories that you created before.
* Get monthly/yearly Reports of your expenses, based on filters that you provide.
* Create Triggers to alarm for things like, “spending too much money on coffee… as always..
```

## Developing

Here's a brief intro about what a developer must do in order to start developing
the project further:

```shell
git clone https://github.com/hojjat1369/ExpenseTracker.git expenseTracker
cd expenseTracker
```

### Building / Deploying

This project needs Java 21 and builds with Spring Boot 3 (`mvn package`).

To run this project after getting source code, simple go to ir.expense.tracker.makharej package and open MakharejApplication.java and run main method.

* If you open application.yml file you can see database configuraion with local url so if you want to run this project you must change this config to your local database.

## Features

* Signup/Login/Get data about your user.
* Ceate/Update/Delete/Get/Search differnt tpyes of Categories for yourself.
* Create/Update/Delete/Get/Search you expenses based of different categories that you created before.
* Get monthly/yearly Reports of your expenses, based on filters that you provide.
* Create Triggers to alarm for things like, “spending too much money on coffee… as always..

Below I explained about important webservies:

#### Signup
Create a user with given username and password.

```bash
Post
URI:/api/auth/signup
Request:
{
  "name": "string",
  "password": "string",
  "username": "string"
}
Response:
{
  "id": 0,
  "name": "string",
  "username": "string"
}
```

#### Login
Login user with credential. This webservice returns a jwt token and for every next webservices we need this token.

* For calling other webservices we should copy the token and put it in Authorization header with Bearer token type

```bash
Post
URI:/api/auth/login
Request:
{
  "password": "string",
  "username": "string"
}
Response:
{
  "token": "string",
  "username": "string"
}
```

#### Create Category
Create a category for logged in user with given name.

```bash
Post
URI: /api/category
Request:
{
  "name": "string"
}
Response:
{
  "id": 0,
  "name": "string"
}
```

#### Update Category
Update a category.

```bash
Patch
URI: /api/category
Request:
{
  "id": 0,
  "name": "string"
}
Response:
{
  "id": 0,
  "name": "string"
}
```

#### Search Category
Search your categories with given name. It returns all categories which their name is similar to given name. Response is a list of searched categories

```bash
Patch
URI: /api/category/find
Request:
{
  "name": "string",
}
Response:
[
  {
    "id": 0,
    "name": "string"
  }
]
```

#### Create Expense
Create a expense for logged in user with given name and other attributes for selected category.

```bash
Post
URI: /api/expense
{
  "amount": 0,
  "categoryId": 0,
  "expenseDate": "2023-05-03 12:12:12",
  "name": "string",
  "note": "string",
  "tag": "string",
}
Response:
{
  "amount": 0,
  "categoryId": 0,
  "expenseDate": "2023-05-03 11:11:11",
  "id": 0,
  "name": "string",
  "note": "string",
  "tag": "string"
}
```

#### Search Expenses
Search expenses based on date (fromDate, toDate), name(like), tag(like), category. Response is a list of searched Expenses. 

```bash
Post
URI: /api/expense/find
{
  "categoryId": 0,
  "fromDate": "2021-05-03 11:11:11",
  "name": "string",
  "tag": "string",
  "toDate": "2023-05-03 11:11:11"
}
Response:
[
  {
    "amount": 0,
    "categoryId": 0,
    "expenseDate": "2022-05-03 11:11:11",
    "id": 0,
    "name": "string",
    "note": "string",
    "tag": "string"
  }
]
```

#### Reports
Monthly or yearly totals of your expenses. It takes the same filters as searching expenses (all of them optional), plus the `period`: `MONTH` or `YEAR`.

```bash
Post
URI:/api/report
Request:
{
  "period": "MONTH",
  "categoryId": 0,
  "fromDate": "2023-01-01 00:00:00",
  "name": "string",
  "tag": "string",
  "toDate": "2023-12-31 23:59:59"
}
Response:
{
  "period": "MONTH",
  "totalAmount": 170.5,
  "count": 3,
  "periods": [
    { "period": "2023-01", "totalAmount": 150.5, "count": 2 },
    { "period": "2023-02", "totalAmount": 0.0, "count": 0 },
    { "period": "2023-03", "totalAmount": 20.0, "count": 1 }
  ]
}
```

* Periods are listed oldest first, and a period without expenses between the first and the last one is listed with zero. A year is labelled `2023`, a month `2023-05`.
* Months and years are Gregorian calendar ones, counted in Tehran time (the time zone of the dates in this API).
* Deleted expenses, and expenses without a date, are not part of a report.

#### Triggers
A trigger is a budget rule: *the expenses that match these filters add up to more than this limit within the current month (or year)*. Filters are optional (`expenseName` is part of an expense name, `tag` is part of a tag, `categoryId` is one of your categories).

```bash
Post
URI:/api/trigger
Request:
{
  "name": "too much coffee",
  "period": "MONTH",
  "limit": 50,
  "tag": "coffee",
  "expenseName": "string",
  "categoryId": 0
}
Response:
{
  "id": 0,
  "name": "too much coffee",
  "period": "MONTH",
  "limit": 50.0,
  "expenseName": null,
  "tag": "coffee",
  "categoryId": null,
  "currentPeriod": "2023-05",
  "currentAmount": 60.0,
  "triggered": true
}
```

* `GET /api/trigger?id=0` returns one trigger, `PATCH /api/trigger` replaces the rule of one (send all of it, plus the `id`), `DELETE /api/trigger?id=0` deletes one.
* `POST /api/trigger/find` lists your triggers. The body is optional filters: `{"name": "coffee", "triggered": true}`. `"triggered": true` gives the alarms that are going off right now.
* Nothing is pushed to you (no e-mail, no notification): the alarm is `triggered`, worked out each time you read a trigger.
* `currentAmount` is what the matching expenses add up to in the current month or year (Tehran time, like the reports), and `triggered` is true when it is more than `limit`.


## Links

For documentaion about APIs, after running this app, you can visit:

SwaggerUrl: http://localhost:8081/swagger-ui.html

The OpenAPI description is served at http://localhost:8081/v3/api-docs


## Licensing
"The code in this project is licensed under HojGod license."  :)
