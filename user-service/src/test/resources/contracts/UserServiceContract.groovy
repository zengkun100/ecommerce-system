package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return even when number input is even"
    request {
        method POST()
        url("/users/register")
        headers {
            contentType(applicationJson())
        }
        body(   "id": 1,
            "email": "a@123.com",
            "password": "123",
            "role": "admin")
    }
    response {
        body("code": 0)
        status 200
    }
}
