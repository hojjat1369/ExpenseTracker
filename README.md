
# Expense Tracker
> A module to track your expenses

This repository contains the Expense Tracker source code. Expense Tracker is a Java module developed to ease the process of tracking daily expenses. It's powerful, actively maintained and easy to use.”

TODO:

* Add more unit tests
* Add integration tests
* Dokerise the project
* Add Trigger entity to project

## Getting started

This Expense Tracker provides simple APIs to work with different types of expense. You can:

```shell
* Signup/Login/Get data about your user.
* Ceate/Update/Delete/Get/Search differnt tpyes of Categories for yourself.
* Create/Update/Delete/Get/Search you expenses based of different categories that you created before.
* TODO: Create different monthly/yearly Reports based on filters that you provide.
* TODO: Create different Triggers to alarm for things like, “spending too much money on coffee… as always..
```

## Developing

Here's a brief intro about what a developer must do in order to start developing
the project further:

```shell
git clone https://github.com/hojjat1369/ExpenseTracker.git expenseTracker
cd expenseTracker
```

### Building / Deploying

To run this project after getting source code, simple go to ir.expense.tracker.makharej package and open MakharejApplication.java and run main method

## Features

* Signup/Login/Get data about your user.
* Ceate/Update/Delete/Get/Search differnt tpyes of Categories for yourself.
* Create/Update/Delete/Get/Search you expenses based of different categories that you created before.
* TODO: Create different monthly/yearly Reports based on filters that you provide.
* TODO: Create different Triggers to alarm for things like, “spending too much money on coffee… as always..

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


## Links

For documentaion about APIs, after running this app, you can visit:

SwaggerUrl: http://localhost:8081/swagger-ui.html#/


## Licensing
"The code in this project is licensed under HojGod license."  :)
