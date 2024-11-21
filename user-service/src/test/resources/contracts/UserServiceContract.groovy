package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return even when number input is even"
    request {
        method POST()
        url("/users/register") {
            queryParameters {
                parameter("username", "admin")
                parameter("password", "password")
                parameter("email", "a@123.com")
                parameter("role", "admin")
            }
        }
    }
    response {
        body(    "id": 1,
                "email": "a@123.com",
                "role": "admin")
        status 200
    }
}
