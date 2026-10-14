import http from 'k6/http';
import { check } from 'k6';

const BASE_URL =
    __ENV.BASE_URL || 'http://host.docker.internal:8080';

const jsonHeaders = {
    headers: {
        'Content-Type': 'application/json',
    },
};

export const options = {
    scenarios: {
        wallet_read_load: {
            executor: 'ramping-arrival-rate',

            startRate: 10,
            timeUnit: '1s',

            preAllocatedVUs: 20,
            maxVUs: 200,

            stages: [
                { duration: '20s', target: 25 },
                { duration: '20s', target: 25 },

                { duration: '20s', target: 50 },
                { duration: '20s', target: 50 },

                { duration: '20s', target: 100 },
                { duration: '20s', target: 100 },

                { duration: '20s', target: 200 },
                { duration: '30s', target: 200 },

                { duration: '10s', target: 0 },
            ],
        },
    },

    thresholds: {
        checks: ['rate>0.99'],
        http_req_failed: ['rate<0.01'],
        http_req_duration: [
            'p(95)<500',
            'p(99)<1000',
        ],
    },
};

export function setup() {
    const response = http.post(
        `${BASE_URL}/api/wallets`,
        JSON.stringify({
            ownerId: `load-test-${Date.now()}`,
            initialBalance: 1000000,
        }),
        jsonHeaders
    );

    const created = check(response, {
        'wallet creation returns 2xx': (res) =>
            res.status === 200 || res.status === 201,

        'wallet response contains id': (res) =>
            res.json('id') !== undefined,
    });

    if (!created) {
        throw new Error(
            `Cannot create wallet. Status=${response.status}, body=${response.body}`
        );
    }

    return {
        walletId: response.json('id'),
    };
}

export default function (data) {
    const response = http.get(
        `${BASE_URL}/api/wallets/${data.walletId}`
    );

    check(response, {
        'GET wallet returns 200': (res) =>
            res.status === 200,

        'wallet id is correct': (res) =>
            res.json('id') === data.walletId,

        'wallet status is ACTIVE': (res) =>
            res.json('status') === 'ACTIVE',
    });
}