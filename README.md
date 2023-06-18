# PTIVS API Documentation

The PTIVS API is used to request student data from a school and organize and return it.

The base URL for all API requests is https://api.xserver.tw/ptivs/. Different behaviors can be achieved by using
different parameters and request methods.


---

## Step 1: Login

To log in and obtain a token, perform a POST request to the following endpoint:

Endpoint: https://api.xserver.tw/ptivs/login/

### Request Parameters

| Parameter | Type   | Description                 |
|:----------|:-------|:----------------------------|
| id        | string | User ID for school website  |
| pwd       | string | Password for school website |

### Request Example

```http
POST https://api.xserver.tw/ptivs/login/
Content-Type: application/json

{
  "id": "your_id",
  "pwd": "your_password"
}
```

### Response Example

```json
{
  "success": true,
  "time": "2023-05-29T10:47:17.893578",
  "token": "3d6615496e73a8b0a8f054da422be5cacaf0b1cb823ffe58b6e9fe9cc09dd6ca5a2b50f01a262573586063b7e361b84115e6e650201ac7c5656882728c2ce8dc"
}
```

---

## Step 2: Get Data

To retrieve the club information, perform a GET request to the following endpoint:

Endpoint: https://api.xserver.tw/ptivs/get/[type]/

| 請求資料                | 用途    | Endpoint Type       |
|---------------------|-------|---------------------|
| rewards             | 學期獎懲  | rewards             |
| history_rewards     | 歷年獎懲  | history_rewards     |
| punished_cancel_log | 銷過紀錄  | punished_cancel_log |
| clubs               | 參與社團  | clubs               |
| cadres              | 擔任幹部  | cadres              |
| history_score       | 歷年成績  | history_score       |
| class_table         | 課表    | class_table         |
| absent              | 學期缺曠課 | absent              |
| history_absent      | 歷年缺曠課 | Not available yet   |
| score               | 學期成績  | Not available yet   |

### Request Headers

| Header | Value                     |
|--------|---------------------------|
| Cookie | token=<token_from_step_1> |

### Request Example (Type: clubs)

```http
GET https://api.xserver.tw/ptivs/get/clubs/
Cookie: token=your_token
```

### Response Example

```json
{
  "data": {
    "參與社團": [
      {
        "年度": 110,
        "學期": 1,
        "社團名稱": "網頁設計",
        "社團組別": "A",
        "擔任職位": "社員",
        "社團成績": 4
      },
      {
        "年度": 110,
        "學期": 2,
        "社團名稱": "網頁設計",
        "社團組別": "A",
        "擔任職位": "社員",
        "社團成績": 4
      },
      {
        "年度": 111,
        "學期": 1,
        "社團名稱": "網頁設計",
        "社團組別": "A",
        "擔任職位": "社長",
        "社團成績": 4
      },
      {
        "年度": 111,
        "學期": 2,
        "社團名稱": "網頁設計",
        "社團組別": "A",
        "擔任職位": "社長",
        "社團成績": 0
      }
    ],
    "profile": {
      "姓名": "黃宥維",
      "班級": "資訊二甲",
      "學年": 111,
      "學期": 2,
      "學號": "013129"
    }
  },
  "success": true,
  "time": "2023-06-16T17:51:55.011435400"
}
```

---

## Error Responses

If the API is used incorrectly, the success field in the response JSON will be false, and additional error or warning
parameters may be present.

### Error Response Example

```json
{
  "success": false,
  "time": "2023-05-29T10:49:19.133941",
  "errors": "cannot get cookie, please POST 'id' and 'pwd' to '/ptivs/login/' for login first"
}
```

Note: The API responses provided in the documentation are examples and may not reflect the actual behavior or data from
the PTIVS API.

---

## Version Change

### 1.0

* Many features, details forgotten.

### 2.0

* HTTP to HTTPS redirection
* Domain renewal for xserver.tw
* Traffic and proxy protection through Cloudflare
* SSL/TLS certificate registration
* Request rate limiting
* Obtaining real IP addresses
* Modular programming
* Restricted access to specified paths (Domain Limit)
* Switched to GET and POST methods
* Optimized login process using cookies
* User caching
* Support for favicon.ico
* README.md beautify
* Line App init support

### 3.0

* Database support
* Line App support
* Content language change to zh-TW
* All data cached
* Improve response speed rate

> Please note that the PTIVS API has undergone significant updates and improvements in version 3.0.

> Note: The above information is provided as a brief summary of the updates made in version 3.0. For detailed
> documentation on each feature and endpoint, please refer to the relevant sections in the API documentation.

---

## Debug

### Curl Tool

Example:

```bash
curl -X POST -d "id=013129&pwd=A123456789" https://api.xserver.tw/ptivs/login/
curl -X GET --cookie "token=替換為cookie值" https://api.xserver.tw/ptivs/get/clubs/
```


---

Author: Huang-You-Wei (Discord: xs._.b)