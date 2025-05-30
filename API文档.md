---
title: 个人项目
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# 个人项目

Base URLs:

# Authentication

- HTTP Authentication, scheme: bearer

# SummerProject/支付控制器

## POST 创建支付订单

POST /api/payment/create

> Body 请求参数

```json
{
  "orderId": 0,
  "userId": 0,
  "amount": 1,
  "paymentMethod": 0,
  "remark": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[PaymentDTO](#schemapaymentdto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "orderId": 0,
    "userId": 0,
    "paymentNo": "",
    "thirdPartyPaymentNo": "",
    "amount": 0,
    "paymentMethod": 0,
    "paymentMethodName": "",
    "status": 0,
    "statusName": "",
    "paymentTime": "",
    "createTime": "",
    "updateTime": "",
    "remark": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultPaymentVO](#schemaresultpaymentvo)|

## POST 支付回调接口

POST /api/payment/callback

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|paymentNo|query|string| 是 |none|
|thirdPartyPaymentNo|query|string| 是 |none|
|status|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 查询支付详情

GET /api/payment/detail/{paymentNo}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|paymentNo|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "orderId": 0,
    "userId": 0,
    "paymentNo": "",
    "thirdPartyPaymentNo": "",
    "amount": 0,
    "paymentMethod": 0,
    "paymentMethodName": "",
    "status": 0,
    "statusName": "",
    "paymentTime": "",
    "createTime": "",
    "updateTime": "",
    "remark": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultPaymentVO](#schemaresultpaymentvo)|

## GET 根据订单ID查询支付记录

GET /api/payment/order/{orderId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|orderId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "orderId": 0,
    "userId": 0,
    "paymentNo": "",
    "thirdPartyPaymentNo": "",
    "amount": 0,
    "paymentMethod": 0,
    "paymentMethodName": "",
    "status": 0,
    "statusName": "",
    "paymentTime": "",
    "createTime": "",
    "updateTime": "",
    "remark": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultPaymentVO](#schemaresultpaymentvo)|

## GET 分页查询用户支付记录

GET /api/payment/user/{userId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer| 是 |none|
|page|query|integer| 是 |none|
|size|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "records": [
      {
        "id": 0,
        "orderId": 0,
        "userId": 0,
        "paymentNo": "",
        "thirdPartyPaymentNo": "",
        "amount": 0,
        "paymentMethod": 0,
        "paymentMethodName": "",
        "status": 0,
        "statusName": "",
        "paymentTime": "",
        "createTime": "",
        "updateTime": "",
        "remark": ""
      }
    ],
    "total": 0,
    "page": 0,
    "size": 0
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultPageResultPaymentVO](#schemaresultpageresultpaymentvo)|

## POST 申请退款

POST /api/payment/refund

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|paymentNo|query|string| 是 |none|
|refundAmount|query|string| 是 |none|
|refundReason|query|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST 退款回调接口

POST /api/payment/refund/callback

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|refundNo|query|string| 是 |none|
|status|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST 取消支付

POST /api/payment/cancel/{paymentNo}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|paymentNo|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

# SummerProject/商品控制器

## POST 添加商品

POST /api/product/create

> Body 请求参数

```json
{
  "categoryId": 0,
  "name": "string",
  "subtitle": "string",
  "mainImage": "string",
  "subImages": "string",
  "detail": "string",
  "price": 0,
  "stock": 0,
  "status": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[ProductDTO](#schemaproductdto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## PUT 更新商品

PUT /api/product/{id}

> Body 请求参数

```json
{
  "categoryId": 0,
  "name": "string",
  "subtitle": "string",
  "mainImage": "string",
  "subImages": "string",
  "detail": "string",
  "price": 0,
  "stock": 0,
  "status": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|
|body|body|[ProductDTO](#schemaproductdto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## DELETE 删除商品

DELETE /api/product/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 获取商品详情

GET /api/product/detail/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "categoryId": 0,
    "categoryName": "",
    "name": "",
    "subtitle": "",
    "mainImage": "",
    "subImages": "",
    "detail": "",
    "price": 0,
    "stock": 0,
    "status": 0,
    "createTime": "",
    "updateTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultProductVO](#schemaresultproductvo)|

## GET 分页查询商品列表

GET /api/product/list

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|page|query|integer| 是 |none|
|size|query|integer| 是 |none|
|categoryId|query|integer| 否 |none|
|keyword|query|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "records": [
      {
        "id": 0,
        "categoryId": 0,
        "categoryName": "",
        "name": "",
        "subtitle": "",
        "mainImage": "",
        "subImages": "",
        "detail": "",
        "price": 0,
        "stock": 0,
        "status": 0,
        "createTime": "",
        "updateTime": ""
      }
    ],
    "total": 0,
    "page": 0,
    "size": 0
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultPageResultProductVO](#schemaresultpageresultproductvo)|

## PUT 更新商品状态

PUT /api/product/{id}/status

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|
|status|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST 扣减库存（内部接口）

POST /api/product/{id}/decrease-stock

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|
|quantity|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": false
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultBoolean](#schemaresultboolean)|

## POST 增加库存（内部接口）

POST /api/product/{id}/increase-stock

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|
|quantity|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": false
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultBoolean](#schemaresultboolean)|

# SummerProject/用户控制器

## POST 用户注册

POST /api/user/register

> Body 请求参数

```json
{
  "username": "string",
  "password": "string",
  "phone": "string",
  "email": "string",
  "nickname": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserDTO](#schemauserdto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "username": "",
    "phone": "",
    "email": "",
    "nickname": "",
    "avatar": "",
    "gender": 0,
    "birthday": "",
    "status": 0,
    "lastLoginTime": "",
    "createTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserVO](#schemaresultuservo)|

## POST 用户登录

POST /api/user/login

> Body 请求参数

```json
{
  "username": "string",
  "password": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[LoginDTO](#schemalogindto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "token": "",
    "userId": 0,
    "username": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultLoginVO](#schemaresultloginvo)|

## GET 获取用户信息

GET /api/user/profile/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "username": "",
    "phone": "",
    "email": "",
    "nickname": "",
    "avatar": "",
    "gender": 0,
    "birthday": "",
    "status": 0,
    "lastLoginTime": "",
    "createTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserVO](#schemaresultuservo)|

## PUT 更新用户信息

PUT /api/user/{id}

> Body 请求参数

```json
{
  "username": "string",
  "password": "string",
  "phone": "string",
  "email": "string",
  "nickname": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|
|body|body|[UserDTO](#schemauserdto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## PUT 修改密码

PUT /api/user/{id}/password

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|
|oldPassword|query|string| 是 |none|
|newPassword|query|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 检查用户名是否存在

GET /api/user/check/username/{username}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": false
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultBoolean](#schemaresultboolean)|

## GET 检查手机号是否存在

GET /api/user/check/phone/{phone}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|phone|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": false
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultBoolean](#schemaresultboolean)|

## GET 通过用户名获取用户信息

GET /api/user/username/{username}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "username": "",
    "phone": "",
    "email": "",
    "nickname": "",
    "avatar": "",
    "gender": 0,
    "birthday": "",
    "status": 0,
    "lastLoginTime": "",
    "createTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserVO](#schemaresultuservo)|

# SummerProject/订单控制器

## GET 根据订单号查询订单详情

GET /api/order/detail/{orderNo}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|orderNo|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "orderNo": "",
    "userId": 0,
    "activityId": 0,
    "productId": 0,
    "flashSaleProductId": 0,
    "productName": "",
    "productImage": "",
    "originalPrice": 0,
    "flashSalePrice": 0,
    "quantity": 0,
    "couponId": 0,
    "discountAmount": 0,
    "payAmount": 0,
    "status": 0,
    "payType": 0,
    "payTime": "",
    "expireTime": "",
    "createTime": "",
    "updateTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultFlashSaleOrder](#schemaresultflashsaleorder)|

## GET 根据用户ID查询订单列表

GET /api/order/user/{userId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "orderNo": "",
      "userId": 0,
      "activityId": 0,
      "productId": 0,
      "flashSaleProductId": 0,
      "productName": "",
      "productImage": "",
      "originalPrice": 0,
      "flashSalePrice": 0,
      "quantity": 0,
      "couponId": 0,
      "discountAmount": 0,
      "payAmount": 0,
      "status": 0,
      "payType": 0,
      "payTime": "",
      "expireTime": "",
      "createTime": "",
      "updateTime": ""
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListFlashSaleOrder](#schemaresultlistflashsaleorder)|

## GET 查询用户待付款订单

GET /api/order/user/{userId}/pending

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "orderNo": "",
      "userId": 0,
      "activityId": 0,
      "productId": 0,
      "flashSaleProductId": 0,
      "productName": "",
      "productImage": "",
      "originalPrice": 0,
      "flashSalePrice": 0,
      "quantity": 0,
      "couponId": 0,
      "discountAmount": 0,
      "payAmount": 0,
      "status": 0,
      "payType": 0,
      "payTime": "",
      "expireTime": "",
      "createTime": "",
      "updateTime": ""
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListFlashSaleOrder](#schemaresultlistflashsaleorder)|

## POST 支付订单

POST /api/order/{orderNo}/pay

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|orderNo|path|string| 是 |none|
|payType|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST 取消订单

POST /api/order/{orderNo}/cancel

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|orderNo|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

# SummerProject/秒杀控制器

## POST 提交秒杀请求

POST /api/seckill/submit

> Body 请求参数

```json
{
  "userId": 0,
  "flashSaleProductId": 0,
  "quantity": 1,
  "token": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[SeckillDTO](#schemaseckilldto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultString](#schemaresultstring)|

## GET 查询秒杀结果

GET /api/seckill/result/{seckillId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|seckillId|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultString](#schemaresultstring)|

## POST 生成秒杀令牌

POST /api/seckill/token/generate

> Body 请求参数

```json
{
  "flashSaleProductId": 0,
  "userId": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[SeckillTokenRequest](#schemaseckilltokenrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultString](#schemaresultstring)|

## POST 创建秒杀活动

POST /api/seckill/activity/create

> Body 请求参数

```json
{
  "name": "string",
  "description": "string",
  "startTime": "string",
  "endTime": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[FlashSaleActivityDTO](#schemaflashsaleactivitydto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 查询秒杀活动列表

GET /api/seckill/activity/list

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|page|query|integer| 是 |none|
|size|query|integer| 是 |none|
|status|query|integer| 否 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": {
    "total": 0,
    "pages": 0,
    "current": 0,
    "size": 0,
    "list": [
      {
        "id": 0,
        "name": "",
        "description": "",
        "startTime": "",
        "endTime": "",
        "status": 0,
        "statusName": "",
        "createTime": "",
        "updateTime": "",
        "products": [
          {
            "id": 0,
            "activityId": 0,
            "productId": 0,
            "productName": "",
            "productImage": "",
            "originalPrice": 0,
            "flashSalePrice": 0,
            "flashSaleStock": 0,
            "flashSaleLimit": 0,
            "stockUsed": 0,
            "remainingStock": 0,
            "startTime": "",
            "endTime": "",
            "status": 0,
            "statusName": "",
            "canSeckill": false,
            "progress": 0,
            "createTime": "",
            "updateTime": ""
          }
        ]
      }
    ]
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultPageResultFlashSaleActivityVO](#schemaresultpageresultflashsaleactivityvo)|

## GET 获取活动详情

GET /api/seckill/activity/detail/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "name": "",
    "description": "",
    "startTime": "",
    "endTime": "",
    "status": 0,
    "statusName": "",
    "createTime": "",
    "updateTime": "",
    "products": [
      {
        "id": 0,
        "activityId": 0,
        "productId": 0,
        "productName": "",
        "productImage": "",
        "originalPrice": 0,
        "flashSalePrice": 0,
        "flashSaleStock": 0,
        "flashSaleLimit": 0,
        "stockUsed": 0,
        "remainingStock": 0,
        "startTime": "",
        "endTime": "",
        "status": 0,
        "statusName": "",
        "canSeckill": false,
        "progress": 0,
        "createTime": "",
        "updateTime": ""
      }
    ]
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultFlashSaleActivityVO](#schemaresultflashsaleactivityvo)|

## POST 启动活动

POST /api/seckill/activity/{id}/start

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST 停止活动

POST /api/seckill/activity/{id}/stop

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST 添加秒杀商品

POST /api/seckill/product/create

> Body 请求参数

```json
{
  "activityId": 0,
  "productId": 0,
  "flashSalePrice": 1,
  "flashSaleStock": 1,
  "flashSaleLimit": 1,
  "startTime": "string",
  "endTime": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[FlashSaleProductDTO](#schemaflashsaleproductdto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 获取商品详情

GET /api/seckill/product/detail/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "activityId": 0,
    "productId": 0,
    "productName": "",
    "productImage": "",
    "originalPrice": 0,
    "flashSalePrice": 0,
    "flashSaleStock": 0,
    "flashSaleLimit": 0,
    "stockUsed": 0,
    "remainingStock": 0,
    "startTime": "",
    "endTime": "",
    "status": 0,
    "statusName": "",
    "canSeckill": false,
    "progress": 0,
    "createTime": "",
    "updateTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultFlashSaleProductVO](#schemaresultflashsaleproductvo)|

## GET 获取活动商品列表

GET /api/seckill/product/list

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|activityId|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "activityId": 0,
      "productId": 0,
      "productName": "",
      "productImage": "",
      "originalPrice": 0,
      "flashSalePrice": 0,
      "flashSaleStock": 0,
      "flashSaleLimit": 0,
      "stockUsed": 0,
      "remainingStock": 0,
      "startTime": "",
      "endTime": "",
      "status": 0,
      "statusName": "",
      "canSeckill": false,
      "progress": 0,
      "createTime": "",
      "updateTime": ""
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListFlashSaleProductVO](#schemaresultlistflashsaleproductvo)|

## GET 检查用户是否可以参与秒杀

GET /api/seckill/check/{userId}/{flashSaleProductId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer| 是 |none|
|flashSaleProductId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": false
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultBoolean](#schemaresultboolean)|

## POST 预热秒杀数据

POST /api/seckill/preload/{activityId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|activityId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 获取秒杀库存

GET /api/seckill/stock/{flashSaleProductId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|flashSaleProductId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultInteger](#schemaresultinteger)|

## GET 获取用户秒杀订单

GET /api/seckill/order/user/{userId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer| 是 |none|
|status|query|integer| 否 |none|
|page|query|integer| 是 |none|
|size|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": {
    "total": 0,
    "pages": 0,
    "current": 0,
    "size": 0,
    "list": [
      {
        "id": 0,
        "orderNo": "",
        "userId": 0,
        "activityId": 0,
        "activityName": "",
        "productId": 0,
        "flashSaleProductId": 0,
        "productName": "",
        "productImage": "",
        "originalPrice": 0,
        "flashSalePrice": 0,
        "quantity": 0,
        "couponId": 0,
        "couponName": "",
        "discountAmount": 0,
        "payAmount": 0,
        "status": 0,
        "statusDesc": "",
        "payType": 0,
        "payTypeDesc": "",
        "payTime": "",
        "expireTime": "",
        "createTime": "",
        "updateTime": "",
        "remainPayTime": 0
      }
    ]
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultPageResultSeckillOrderVO](#schemaresultpageresultseckillordervo)|

## POST 支付秒杀订单

POST /api/seckill/order/{orderNo}/pay

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|orderNo|path|string| 是 |none|
|userId|query|integer| 是 |none|
|payType|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultString](#schemaresultstring)|

## GET 获取订单详情

GET /api/seckill/order/{orderNo}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|orderNo|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "success": false,
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "orderNo": "",
    "userId": 0,
    "activityId": 0,
    "activityName": "",
    "productId": 0,
    "flashSaleProductId": 0,
    "productName": "",
    "productImage": "",
    "originalPrice": 0,
    "flashSalePrice": 0,
    "quantity": 0,
    "couponId": 0,
    "couponName": "",
    "discountAmount": 0,
    "payAmount": 0,
    "status": 0,
    "statusDesc": "",
    "payType": 0,
    "payTypeDesc": "",
    "payTime": "",
    "expireTime": "",
    "createTime": "",
    "updateTime": "",
    "remainPayTime": 0
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultSeckillOrderVO](#schemaresultseckillordervo)|

# 数据模型

<h2 id="tocS_FlashSaleOrder">FlashSaleOrder</h2>

<a id="schemaflashsaleorder"></a>
<a id="schema_FlashSaleOrder"></a>
<a id="tocSflashsaleorder"></a>
<a id="tocsflashsaleorder"></a>

```json
{
  "id": 0,
  "orderNo": "string",
  "userId": 0,
  "activityId": 0,
  "productId": 0,
  "flashSaleProductId": 0,
  "productName": "string",
  "productImage": "string",
  "originalPrice": 0,
  "flashSalePrice": 0,
  "quantity": 0,
  "couponId": 0,
  "discountAmount": 0,
  "payAmount": 0,
  "status": 0,
  "payType": 0,
  "payTime": "string",
  "expireTime": "string",
  "createTime": "string",
  "updateTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||订单ID|
|orderNo|string|false|none||订单编号|
|userId|integer(int64)|false|none||用户ID|
|activityId|integer(int64)|false|none||活动ID|
|productId|integer(int64)|false|none||商品ID|
|flashSaleProductId|integer(int64)|false|none||秒杀商品ID|
|productName|string|false|none||商品名称|
|productImage|string|false|none||商品主图|
|originalPrice|number|false|none||原价|
|flashSalePrice|number|false|none||秒杀价格|
|quantity|integer|false|none||购买数量|
|couponId|integer(int64)|false|none||优惠券ID|
|discountAmount|number|false|none||优惠金额|
|payAmount|number|false|none||实付金额|
|status|integer|false|none||订单状态：0-待支付，1-已支付，2-已取消，3-已退款，4-已完成|
|payType|integer|false|none||支付方式：1-支付宝，2-微信，3-银行卡|
|payTime|string|false|none||支付时间|
|expireTime|string|false|none||过期时间|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|

<h2 id="tocS_ResultFlashSaleOrder">ResultFlashSaleOrder</h2>

<a id="schemaresultflashsaleorder"></a>
<a id="schema_ResultFlashSaleOrder"></a>
<a id="tocSresultflashsaleorder"></a>
<a id="tocsresultflashsaleorder"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "orderNo": "string",
    "userId": 0,
    "activityId": 0,
    "productId": 0,
    "flashSaleProductId": 0,
    "productName": "string",
    "productImage": "string",
    "originalPrice": 0,
    "flashSalePrice": 0,
    "quantity": 0,
    "couponId": 0,
    "discountAmount": 0,
    "payAmount": 0,
    "status": 0,
    "payType": 0,
    "payTime": "string",
    "expireTime": "string",
    "createTime": "string",
    "updateTime": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[FlashSaleOrder](#schemaflashsaleorder)|false|none||数据|

<h2 id="tocS_PaymentDTO">PaymentDTO</h2>

<a id="schemapaymentdto"></a>
<a id="schema_PaymentDTO"></a>
<a id="tocSpaymentdto"></a>
<a id="tocspaymentdto"></a>

```json
{
  "orderId": 0,
  "userId": 0,
  "amount": 1,
  "paymentMethod": 0,
  "remark": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|orderId|integer(int64)|true|none||订单ID|
|userId|integer(int64)|true|none||用户ID|
|amount|number|true|none||支付金额|
|paymentMethod|integer|true|none||支付方式：1-支付宝，2-微信，3-银行卡|
|remark|string|false|none||备注|

<h2 id="tocS_ResultListFlashSaleOrder">ResultListFlashSaleOrder</h2>

<a id="schemaresultlistflashsaleorder"></a>
<a id="schema_ResultListFlashSaleOrder"></a>
<a id="tocSresultlistflashsaleorder"></a>
<a id="tocsresultlistflashsaleorder"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "id": 0,
      "orderNo": "string",
      "userId": 0,
      "activityId": 0,
      "productId": 0,
      "flashSaleProductId": 0,
      "productName": "string",
      "productImage": "string",
      "originalPrice": 0,
      "flashSalePrice": 0,
      "quantity": 0,
      "couponId": 0,
      "discountAmount": 0,
      "payAmount": 0,
      "status": 0,
      "payType": 0,
      "payTime": "string",
      "expireTime": "string",
      "createTime": "string",
      "updateTime": "string"
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[[FlashSaleOrder](#schemaflashsaleorder)]|false|none||数据|

<h2 id="tocS_ResultVoid">ResultVoid</h2>

<a id="schemaresultvoid"></a>
<a id="schema_ResultVoid"></a>
<a id="tocSresultvoid"></a>
<a id="tocsresultvoid"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": null
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|null|false|none||数据|

<h2 id="tocS_LoginDTO">LoginDTO</h2>

<a id="schemalogindto"></a>
<a id="schema_LoginDTO"></a>
<a id="tocSlogindto"></a>
<a id="tocslogindto"></a>

```json
{
  "username": "string",
  "password": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|true|none||用户名|
|password|string|true|none||密码|

<h2 id="tocS_PaymentVO">PaymentVO</h2>

<a id="schemapaymentvo"></a>
<a id="schema_PaymentVO"></a>
<a id="tocSpaymentvo"></a>
<a id="tocspaymentvo"></a>

```json
{
  "id": 0,
  "orderId": 0,
  "userId": 0,
  "paymentNo": "string",
  "thirdPartyPaymentNo": "string",
  "amount": 0,
  "paymentMethod": 0,
  "paymentMethodName": "string",
  "status": 0,
  "statusName": "string",
  "paymentTime": "string",
  "createTime": "string",
  "updateTime": "string",
  "remark": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||支付ID|
|orderId|integer(int64)|false|none||订单ID|
|userId|integer(int64)|false|none||用户ID|
|paymentNo|string|false|none||支付流水号|
|thirdPartyPaymentNo|string|false|none||第三方支付流水号|
|amount|number|false|none||支付金额|
|paymentMethod|integer|false|none||支付方式：1-支付宝，2-微信，3-银行卡|
|paymentMethodName|string|false|none||支付方式名称|
|status|integer|false|none||支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款|
|statusName|string|false|none||支付状态名称|
|paymentTime|string|false|none||支付时间|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|
|remark|string|false|none||备注|

<h2 id="tocS_FlashSaleActivityDTO">FlashSaleActivityDTO</h2>

<a id="schemaflashsaleactivitydto"></a>
<a id="schema_FlashSaleActivityDTO"></a>
<a id="tocSflashsaleactivitydto"></a>
<a id="tocsflashsaleactivitydto"></a>

```json
{
  "name": "string",
  "description": "string",
  "startTime": "string",
  "endTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|name|string|true|none||活动名称|
|description|string|false|none||活动描述|
|startTime|string|true|none||开始时间|
|endTime|string|true|none||结束时间|

<h2 id="tocS_ResultPaymentVO">ResultPaymentVO</h2>

<a id="schemaresultpaymentvo"></a>
<a id="schema_ResultPaymentVO"></a>
<a id="tocSresultpaymentvo"></a>
<a id="tocsresultpaymentvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "orderId": 0,
    "userId": 0,
    "paymentNo": "string",
    "thirdPartyPaymentNo": "string",
    "amount": 0,
    "paymentMethod": 0,
    "paymentMethodName": "string",
    "status": 0,
    "statusName": "string",
    "paymentTime": "string",
    "createTime": "string",
    "updateTime": "string",
    "remark": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[PaymentVO](#schemapaymentvo)|false|none||数据|

<h2 id="tocS_FlashSaleProductVO">FlashSaleProductVO</h2>

<a id="schemaflashsaleproductvo"></a>
<a id="schema_FlashSaleProductVO"></a>
<a id="tocSflashsaleproductvo"></a>
<a id="tocsflashsaleproductvo"></a>

```json
{
  "id": 0,
  "activityId": 0,
  "productId": 0,
  "productName": "string",
  "productImage": "string",
  "originalPrice": 0,
  "flashSalePrice": 0,
  "flashSaleStock": 0,
  "flashSaleLimit": 0,
  "stockUsed": 0,
  "remainingStock": 0,
  "startTime": "string",
  "endTime": "string",
  "status": 0,
  "statusName": "string",
  "canSeckill": true,
  "progress": 0,
  "createTime": "string",
  "updateTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||秒杀商品ID|
|activityId|integer(int64)|false|none||活动ID|
|productId|integer(int64)|false|none||商品ID|
|productName|string|false|none||商品名称|
|productImage|string|false|none||商品图片|
|originalPrice|number|false|none||原价|
|flashSalePrice|number|false|none||秒杀价格|
|flashSaleStock|integer|false|none||秒杀库存|
|flashSaleLimit|integer|false|none||每人限购数量|
|stockUsed|integer|false|none||已售数量|
|remainingStock|integer|false|none||剩余库存|
|startTime|string|false|none||开始时间|
|endTime|string|false|none||结束时间|
|status|integer|false|none||状态：0-禁用，1-启用|
|statusName|string|false|none||状态名称|
|canSeckill|boolean|false|none||是否可以秒杀|
|progress|integer|false|none||秒杀进度（百分比）|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|

<h2 id="tocS_ResultInteger">ResultInteger</h2>

<a id="schemaresultinteger"></a>
<a id="schema_ResultInteger"></a>
<a id="tocSresultinteger"></a>
<a id="tocsresultinteger"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|integer|false|none||none|

<h2 id="tocS_FlashSaleActivityVO">FlashSaleActivityVO</h2>

<a id="schemaflashsaleactivityvo"></a>
<a id="schema_FlashSaleActivityVO"></a>
<a id="tocSflashsaleactivityvo"></a>
<a id="tocsflashsaleactivityvo"></a>

```json
{
  "id": 0,
  "name": "string",
  "description": "string",
  "startTime": "string",
  "endTime": "string",
  "status": 0,
  "statusName": "string",
  "createTime": "string",
  "updateTime": "string",
  "products": [
    {
      "id": 0,
      "activityId": 0,
      "productId": 0,
      "productName": "string",
      "productImage": "string",
      "originalPrice": 0,
      "flashSalePrice": 0,
      "flashSaleStock": 0,
      "flashSaleLimit": 0,
      "stockUsed": 0,
      "remainingStock": 0,
      "startTime": "string",
      "endTime": "string",
      "status": 0,
      "statusName": "string",
      "canSeckill": true,
      "progress": 0,
      "createTime": "string",
      "updateTime": "string"
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||活动ID|
|name|string|false|none||活动名称|
|description|string|false|none||活动描述|
|startTime|string|false|none||开始时间|
|endTime|string|false|none||结束时间|
|status|integer|false|none||状态：0-未开始，1-进行中，2-已结束|
|statusName|string|false|none||状态名称|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|
|products|[[FlashSaleProductVO](#schemaflashsaleproductvo)]|false|none||秒杀商品列表|

<h2 id="tocS_PageResultPaymentVO">PageResultPaymentVO</h2>

<a id="schemapageresultpaymentvo"></a>
<a id="schema_PageResultPaymentVO"></a>
<a id="tocSpageresultpaymentvo"></a>
<a id="tocspageresultpaymentvo"></a>

```json
{
  "records": [
    {
      "id": 0,
      "orderId": 0,
      "userId": 0,
      "paymentNo": "string",
      "thirdPartyPaymentNo": "string",
      "amount": 0,
      "paymentMethod": 0,
      "paymentMethodName": "string",
      "status": 0,
      "statusName": "string",
      "paymentTime": "string",
      "createTime": "string",
      "updateTime": "string",
      "remark": "string"
    }
  ],
  "total": 0,
  "page": 0,
  "size": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|records|[[PaymentVO](#schemapaymentvo)]|false|none||none|
|total|integer(int64)|false|none||none|
|page|integer|false|none||none|
|size|integer|false|none||none|

<h2 id="tocS_PageResultFlashSaleActivityVO">PageResultFlashSaleActivityVO</h2>

<a id="schemapageresultflashsaleactivityvo"></a>
<a id="schema_PageResultFlashSaleActivityVO"></a>
<a id="tocSpageresultflashsaleactivityvo"></a>
<a id="tocspageresultflashsaleactivityvo"></a>

```json
{
  "total": 0,
  "pages": 0,
  "current": 0,
  "size": 0,
  "list": [
    {
      "id": 0,
      "name": "string",
      "description": "string",
      "startTime": "string",
      "endTime": "string",
      "status": 0,
      "statusName": "string",
      "createTime": "string",
      "updateTime": "string",
      "products": [
        {
          "id": 0,
          "activityId": 0,
          "productId": 0,
          "productName": "string",
          "productImage": "string",
          "originalPrice": 0,
          "flashSalePrice": 0,
          "flashSaleStock": 0,
          "flashSaleLimit": 0,
          "stockUsed": 0,
          "remainingStock": 0,
          "startTime": "string",
          "endTime": "string",
          "status": 0,
          "statusName": "string",
          "canSeckill": true,
          "progress": 0,
          "createTime": "string",
          "updateTime": "string"
        }
      ]
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|total|integer(int64)|false|none||none|
|pages|integer(int64)|false|none||总页数|
|current|integer(int64)|false|none||当前页码|
|size|integer(int64)|false|none||none|
|list|[[FlashSaleActivityVO](#schemaflashsaleactivityvo)]|false|none||数据列表|

<h2 id="tocS_ResultPageResultPaymentVO">ResultPageResultPaymentVO</h2>

<a id="schemaresultpageresultpaymentvo"></a>
<a id="schema_ResultPageResultPaymentVO"></a>
<a id="tocSresultpageresultpaymentvo"></a>
<a id="tocsresultpageresultpaymentvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "records": [
      {
        "id": 0,
        "orderId": 0,
        "userId": 0,
        "paymentNo": "string",
        "thirdPartyPaymentNo": "string",
        "amount": 0,
        "paymentMethod": 0,
        "paymentMethodName": "string",
        "status": 0,
        "statusName": "string",
        "paymentTime": "string",
        "createTime": "string",
        "updateTime": "string",
        "remark": "string"
      }
    ],
    "total": 0,
    "page": 0,
    "size": 0
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[PageResultPaymentVO](#schemapageresultpaymentvo)|false|none||数据|

<h2 id="tocS_ResultPageResultFlashSaleActivityVO">ResultPageResultFlashSaleActivityVO</h2>

<a id="schemaresultpageresultflashsaleactivityvo"></a>
<a id="schema_ResultPageResultFlashSaleActivityVO"></a>
<a id="tocSresultpageresultflashsaleactivityvo"></a>
<a id="tocsresultpageresultflashsaleactivityvo"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": {
    "total": 0,
    "pages": 0,
    "current": 0,
    "size": 0,
    "list": [
      {
        "id": 0,
        "name": "string",
        "description": "string",
        "startTime": "string",
        "endTime": "string",
        "status": 0,
        "statusName": "string",
        "createTime": "string",
        "updateTime": "string",
        "products": [
          {
            "id": null,
            "activityId": null,
            "productId": null,
            "productName": null,
            "productImage": null,
            "originalPrice": null,
            "flashSalePrice": null,
            "flashSaleStock": null,
            "flashSaleLimit": null,
            "stockUsed": null,
            "remainingStock": null,
            "startTime": null,
            "endTime": null,
            "status": null,
            "statusName": null,
            "canSeckill": null,
            "progress": null,
            "createTime": null,
            "updateTime": null
          }
        ]
      }
    ]
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[PageResultFlashSaleActivityVO](#schemapageresultflashsaleactivityvo)|false|none||数据|

<h2 id="tocS_ProductDTO">ProductDTO</h2>

<a id="schemaproductdto"></a>
<a id="schema_ProductDTO"></a>
<a id="tocSproductdto"></a>
<a id="tocsproductdto"></a>

```json
{
  "categoryId": 0,
  "name": "string",
  "subtitle": "string",
  "mainImage": "string",
  "subImages": "string",
  "detail": "string",
  "price": 0,
  "stock": 0,
  "status": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|categoryId|integer(int64)|true|none||分类ID|
|name|string|true|none||商品名称|
|subtitle|string|false|none||商品副标题|
|mainImage|string|false|none||主图片URL|
|subImages|string|false|none||子图片URL，以逗号分隔|
|detail|string|false|none||商品详情|
|price|number|true|none||原价|
|stock|integer|true|none||库存|
|status|integer|false|none||状态：0-下架，1-上架|

<h2 id="tocS_ResultFlashSaleActivityVO">ResultFlashSaleActivityVO</h2>

<a id="schemaresultflashsaleactivityvo"></a>
<a id="schema_ResultFlashSaleActivityVO"></a>
<a id="tocSresultflashsaleactivityvo"></a>
<a id="tocsresultflashsaleactivityvo"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "name": "string",
    "description": "string",
    "startTime": "string",
    "endTime": "string",
    "status": 0,
    "statusName": "string",
    "createTime": "string",
    "updateTime": "string",
    "products": [
      {
        "id": 0,
        "activityId": 0,
        "productId": 0,
        "productName": "string",
        "productImage": "string",
        "originalPrice": 0,
        "flashSalePrice": 0,
        "flashSaleStock": 0,
        "flashSaleLimit": 0,
        "stockUsed": 0,
        "remainingStock": 0,
        "startTime": "string",
        "endTime": "string",
        "status": 0,
        "statusName": "string",
        "canSeckill": true,
        "progress": 0,
        "createTime": "string",
        "updateTime": "string"
      }
    ]
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[FlashSaleActivityVO](#schemaflashsaleactivityvo)|false|none||秒杀活动视图对象|

<h2 id="tocS_ProductVO">ProductVO</h2>

<a id="schemaproductvo"></a>
<a id="schema_ProductVO"></a>
<a id="tocSproductvo"></a>
<a id="tocsproductvo"></a>

```json
{
  "id": 0,
  "categoryId": 0,
  "categoryName": "string",
  "name": "string",
  "subtitle": "string",
  "mainImage": "string",
  "subImages": "string",
  "detail": "string",
  "price": 0,
  "stock": 0,
  "status": 0,
  "createTime": "string",
  "updateTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||商品ID|
|categoryId|integer(int64)|false|none||分类ID|
|categoryName|string|false|none||分类名称|
|name|string|false|none||商品名称|
|subtitle|string|false|none||商品副标题|
|mainImage|string|false|none||主图片URL|
|subImages|string|false|none||子图片URL，以逗号分隔|
|detail|string|false|none||商品详情|
|price|number|false|none||原价|
|stock|integer|false|none||库存|
|status|integer|false|none||状态：0-下架，1-上架|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|

<h2 id="tocS_FlashSaleProductDTO">FlashSaleProductDTO</h2>

<a id="schemaflashsaleproductdto"></a>
<a id="schema_FlashSaleProductDTO"></a>
<a id="tocSflashsaleproductdto"></a>
<a id="tocsflashsaleproductdto"></a>

```json
{
  "activityId": 0,
  "productId": 0,
  "flashSalePrice": 1,
  "flashSaleStock": 1,
  "flashSaleLimit": 1,
  "startTime": "string",
  "endTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|activityId|integer(int64)|true|none||活动ID|
|productId|integer(int64)|true|none||商品ID|
|flashSalePrice|number|true|none||秒杀价格|
|flashSaleStock|integer|true|none||秒杀库存|
|flashSaleLimit|integer|true|none||每人限购数量|
|startTime|string|true|none||开始时间|
|endTime|string|true|none||结束时间|

<h2 id="tocS_ResultProductVO">ResultProductVO</h2>

<a id="schemaresultproductvo"></a>
<a id="schema_ResultProductVO"></a>
<a id="tocSresultproductvo"></a>
<a id="tocsresultproductvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "categoryId": 0,
    "categoryName": "string",
    "name": "string",
    "subtitle": "string",
    "mainImage": "string",
    "subImages": "string",
    "detail": "string",
    "price": 0,
    "stock": 0,
    "status": 0,
    "createTime": "string",
    "updateTime": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[ProductVO](#schemaproductvo)|false|none||数据|

<h2 id="tocS_ResultListFlashSaleProductVO">ResultListFlashSaleProductVO</h2>

<a id="schemaresultlistflashsaleproductvo"></a>
<a id="schema_ResultListFlashSaleProductVO"></a>
<a id="tocSresultlistflashsaleproductvo"></a>
<a id="tocsresultlistflashsaleproductvo"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": [
    {
      "id": 0,
      "activityId": 0,
      "productId": 0,
      "productName": "string",
      "productImage": "string",
      "originalPrice": 0,
      "flashSalePrice": 0,
      "flashSaleStock": 0,
      "flashSaleLimit": 0,
      "stockUsed": 0,
      "remainingStock": 0,
      "startTime": "string",
      "endTime": "string",
      "status": 0,
      "statusName": "string",
      "canSeckill": true,
      "progress": 0,
      "createTime": "string",
      "updateTime": "string"
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[[FlashSaleProductVO](#schemaflashsaleproductvo)]|false|none||数据|

<h2 id="tocS_PageResultProductVO">PageResultProductVO</h2>

<a id="schemapageresultproductvo"></a>
<a id="schema_PageResultProductVO"></a>
<a id="tocSpageresultproductvo"></a>
<a id="tocspageresultproductvo"></a>

```json
{
  "records": [
    {
      "id": 0,
      "categoryId": 0,
      "categoryName": "string",
      "name": "string",
      "subtitle": "string",
      "mainImage": "string",
      "subImages": "string",
      "detail": "string",
      "price": 0,
      "stock": 0,
      "status": 0,
      "createTime": "string",
      "updateTime": "string"
    }
  ],
  "total": 0,
  "page": 0,
  "size": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|records|[[ProductVO](#schemaproductvo)]|false|none||none|
|total|integer(int64)|false|none||none|
|page|integer|false|none||none|
|size|integer|false|none||none|

<h2 id="tocS_ResultPageResultProductVO">ResultPageResultProductVO</h2>

<a id="schemaresultpageresultproductvo"></a>
<a id="schema_ResultPageResultProductVO"></a>
<a id="tocSresultpageresultproductvo"></a>
<a id="tocsresultpageresultproductvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "records": [
      {
        "id": 0,
        "categoryId": 0,
        "categoryName": "string",
        "name": "string",
        "subtitle": "string",
        "mainImage": "string",
        "subImages": "string",
        "detail": "string",
        "price": 0,
        "stock": 0,
        "status": 0,
        "createTime": "string",
        "updateTime": "string"
      }
    ],
    "total": 0,
    "page": 0,
    "size": 0
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[PageResultProductVO](#schemapageresultproductvo)|false|none||数据|

<h2 id="tocS_ResultBoolean">ResultBoolean</h2>

<a id="schemaresultboolean"></a>
<a id="schema_ResultBoolean"></a>
<a id="tocSresultboolean"></a>
<a id="tocsresultboolean"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|boolean|false|none||数据|

<h2 id="tocS_ResultFlashSaleProductVO">ResultFlashSaleProductVO</h2>

<a id="schemaresultflashsaleproductvo"></a>
<a id="schema_ResultFlashSaleProductVO"></a>
<a id="tocSresultflashsaleproductvo"></a>
<a id="tocsresultflashsaleproductvo"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "activityId": 0,
    "productId": 0,
    "productName": "string",
    "productImage": "string",
    "originalPrice": 0,
    "flashSalePrice": 0,
    "flashSaleStock": 0,
    "flashSaleLimit": 0,
    "stockUsed": 0,
    "remainingStock": 0,
    "startTime": "string",
    "endTime": "string",
    "status": 0,
    "statusName": "string",
    "canSeckill": true,
    "progress": 0,
    "createTime": "string",
    "updateTime": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||状态码|
|message|string|false|none||消息|
|data|[FlashSaleProductVO](#schemaflashsaleproductvo)|false|none||秒杀商品VO|

<h2 id="tocS_SeckillDTO">SeckillDTO</h2>

<a id="schemaseckilldto"></a>
<a id="schema_SeckillDTO"></a>
<a id="tocSseckilldto"></a>
<a id="tocsseckilldto"></a>

```json
{
  "userId": 0,
  "flashSaleProductId": 0,
  "quantity": 1,
  "token": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userId|integer(int64)|false|none||用户ID|
|flashSaleProductId|integer(int64)|false|none||秒杀商品ID|
|quantity|integer|false|none||购买数量|
|token|string|false|none||用户令牌|

<h2 id="tocS_ResultString">ResultString</h2>

<a id="schemaresultstring"></a>
<a id="schema_ResultString"></a>
<a id="tocSresultstring"></a>
<a id="tocsresultstring"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|string|false|none||none|

<h2 id="tocS_UserDTO">UserDTO</h2>

<a id="schemauserdto"></a>
<a id="schema_UserDTO"></a>
<a id="tocSuserdto"></a>
<a id="tocsuserdto"></a>

```json
{
  "username": "string",
  "password": "string",
  "phone": "string",
  "email": "string",
  "nickname": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|true|none||用户名|
|password|string|true|none||密码|
|phone|string|true|none||手机号|
|email|string|false|none||邮箱|
|nickname|string|false|none||昵称|

<h2 id="tocS_SeckillTokenRequest">SeckillTokenRequest</h2>

<a id="schemaseckilltokenrequest"></a>
<a id="schema_SeckillTokenRequest"></a>
<a id="tocSseckilltokenrequest"></a>
<a id="tocsseckilltokenrequest"></a>

```json
{
  "flashSaleProductId": 0,
  "userId": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|flashSaleProductId|integer(int64)|false|none||none|
|userId|integer(int64)|false|none||none|

<h2 id="tocS_UserVO">UserVO</h2>

<a id="schemauservo"></a>
<a id="schema_UserVO"></a>
<a id="tocSuservo"></a>
<a id="tocsuservo"></a>

```json
{
  "id": 0,
  "username": "string",
  "phone": "string",
  "email": "string",
  "nickname": "string",
  "avatar": "string",
  "gender": 0,
  "birthday": "string",
  "status": 0,
  "lastLoginTime": "string",
  "createTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||用户ID|
|username|string|false|none||用户名|
|phone|string|false|none||手机号|
|email|string|false|none||邮箱|
|nickname|string|false|none||昵称|
|avatar|string|false|none||头像URL|
|gender|integer|false|none||性别：0-未知，1-男，2-女|
|birthday|string|false|none||生日|
|status|integer|false|none||状态：0-禁用，1-启用|
|lastLoginTime|string|false|none||最后登录时间|
|createTime|string|false|none||创建时间|

<h2 id="tocS_ResultUserVO">ResultUserVO</h2>

<a id="schemaresultuservo"></a>
<a id="schema_ResultUserVO"></a>
<a id="tocSresultuservo"></a>
<a id="tocsresultuservo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "username": "string",
    "phone": "string",
    "email": "string",
    "nickname": "string",
    "avatar": "string",
    "gender": 0,
    "birthday": "string",
    "status": 0,
    "lastLoginTime": "string",
    "createTime": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[UserVO](#schemauservo)|false|none||数据|

<h2 id="tocS_SeckillOrderVO">SeckillOrderVO</h2>

<a id="schemaseckillordervo"></a>
<a id="schema_SeckillOrderVO"></a>
<a id="tocSseckillordervo"></a>
<a id="tocsseckillordervo"></a>

```json
{
  "id": 0,
  "orderNo": "string",
  "userId": 0,
  "activityId": 0,
  "activityName": "string",
  "productId": 0,
  "flashSaleProductId": 0,
  "productName": "string",
  "productImage": "string",
  "originalPrice": 0,
  "flashSalePrice": 0,
  "quantity": 0,
  "couponId": 0,
  "couponName": "string",
  "discountAmount": 0,
  "payAmount": 0,
  "status": 0,
  "statusDesc": "string",
  "payType": 0,
  "payTypeDesc": "string",
  "payTime": "string",
  "expireTime": "string",
  "createTime": "string",
  "updateTime": "string",
  "remainPayTime": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||订单ID|
|orderNo|string|false|none||订单编号|
|userId|integer(int64)|false|none||用户ID|
|activityId|integer(int64)|false|none||活动ID|
|activityName|string|false|none||活动名称|
|productId|integer(int64)|false|none||商品ID|
|flashSaleProductId|integer(int64)|false|none||秒杀商品ID|
|productName|string|false|none||商品名称|
|productImage|string|false|none||商品主图|
|originalPrice|number|false|none||原价|
|flashSalePrice|number|false|none||秒杀价格|
|quantity|integer|false|none||购买数量|
|couponId|integer(int64)|false|none||优惠券ID|
|couponName|string|false|none||优惠券名称|
|discountAmount|number|false|none||优惠金额|
|payAmount|number|false|none||实付金额|
|status|integer|false|none||订单状态：0-待支付，1-已支付，2-已取消，3-已退款，4-已完成|
|statusDesc|string|false|none||订单状态描述|
|payType|integer|false|none||支付方式：1-支付宝，2-微信，3-银行卡|
|payTypeDesc|string|false|none||支付方式描述|
|payTime|string|false|none||支付时间|
|expireTime|string|false|none||过期时间|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|
|remainPayTime|integer(int64)|false|none||剩余支付时间（秒）|

<h2 id="tocS_PageResultSeckillOrderVO">PageResultSeckillOrderVO</h2>

<a id="schemapageresultseckillordervo"></a>
<a id="schema_PageResultSeckillOrderVO"></a>
<a id="tocSpageresultseckillordervo"></a>
<a id="tocspageresultseckillordervo"></a>

```json
{
  "total": 0,
  "pages": 0,
  "current": 0,
  "size": 0,
  "list": [
    {
      "id": 0,
      "orderNo": "string",
      "userId": 0,
      "activityId": 0,
      "activityName": "string",
      "productId": 0,
      "flashSaleProductId": 0,
      "productName": "string",
      "productImage": "string",
      "originalPrice": 0,
      "flashSalePrice": 0,
      "quantity": 0,
      "couponId": 0,
      "couponName": "string",
      "discountAmount": 0,
      "payAmount": 0,
      "status": 0,
      "statusDesc": "string",
      "payType": 0,
      "payTypeDesc": "string",
      "payTime": "string",
      "expireTime": "string",
      "createTime": "string",
      "updateTime": "string",
      "remainPayTime": 0
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|total|integer(int64)|false|none||总记录数|
|pages|integer(int64)|false|none||总页数|
|current|integer(int64)|false|none||当前页码|
|size|integer(int64)|false|none||每页大小|
|list|[[SeckillOrderVO](#schemaseckillordervo)]|false|none||数据列表|

<h2 id="tocS_ResultPageResultSeckillOrderVO">ResultPageResultSeckillOrderVO</h2>

<a id="schemaresultpageresultseckillordervo"></a>
<a id="schema_ResultPageResultSeckillOrderVO"></a>
<a id="tocSresultpageresultseckillordervo"></a>
<a id="tocsresultpageresultseckillordervo"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": {
    "total": 0,
    "pages": 0,
    "current": 0,
    "size": 0,
    "list": [
      {
        "id": 0,
        "orderNo": "string",
        "userId": 0,
        "activityId": 0,
        "activityName": "string",
        "productId": 0,
        "flashSaleProductId": 0,
        "productName": "string",
        "productImage": "string",
        "originalPrice": 0,
        "flashSalePrice": 0,
        "quantity": 0,
        "couponId": 0,
        "couponName": "string",
        "discountAmount": 0,
        "payAmount": 0,
        "status": 0,
        "statusDesc": "string",
        "payType": 0,
        "payTypeDesc": "string",
        "payTime": "string",
        "expireTime": "string",
        "createTime": "string",
        "updateTime": "string",
        "remainPayTime": 0
      }
    ]
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||状态码|
|message|string|false|none||消息|
|data|[PageResultSeckillOrderVO](#schemapageresultseckillordervo)|false|none||数据|

<h2 id="tocS_ResultSeckillOrderVO">ResultSeckillOrderVO</h2>

<a id="schemaresultseckillordervo"></a>
<a id="schema_ResultSeckillOrderVO"></a>
<a id="tocSresultseckillordervo"></a>
<a id="tocsresultseckillordervo"></a>

```json
{
  "success": true,
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "orderNo": "string",
    "userId": 0,
    "activityId": 0,
    "activityName": "string",
    "productId": 0,
    "flashSaleProductId": 0,
    "productName": "string",
    "productImage": "string",
    "originalPrice": 0,
    "flashSalePrice": 0,
    "quantity": 0,
    "couponId": 0,
    "couponName": "string",
    "discountAmount": 0,
    "payAmount": 0,
    "status": 0,
    "statusDesc": "string",
    "payType": 0,
    "payTypeDesc": "string",
    "payTime": "string",
    "expireTime": "string",
    "createTime": "string",
    "updateTime": "string",
    "remainPayTime": 0
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|success|boolean|false|none||是否成功|
|code|integer|false|none||状态码|
|message|string|false|none||消息|
|data|[SeckillOrderVO](#schemaseckillordervo)|false|none||秒杀订单视图对象|

<h2 id="tocS_LoginVO">LoginVO</h2>

<a id="schemaloginvo"></a>
<a id="schema_LoginVO"></a>
<a id="tocSloginvo"></a>
<a id="tocsloginvo"></a>

```json
{
  "token": "string",
  "userId": 0,
  "username": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|token|string|false|none||认证令牌|
|userId|integer(int64)|false|none||用户ID|
|username|string|false|none||用户名|

<h2 id="tocS_ResultLoginVO">ResultLoginVO</h2>

<a id="schemaresultloginvo"></a>
<a id="schema_ResultLoginVO"></a>
<a id="tocSresultloginvo"></a>
<a id="tocsresultloginvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "token": "string",
    "userId": 0,
    "username": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||信息|
|data|[LoginVO](#schemaloginvo)|false|none||数据|

