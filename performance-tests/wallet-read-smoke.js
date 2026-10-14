import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL =
    __ENV.BASE_URL || 'http://host.docker.internal:8080';

const jsonHeaders = {
    headers: {
        'Content-Type': 'application/json',
    },
};

export const options = {
    scenarios: {
        smoke: {
            executor: 'constant-vus',
            vus: 1,
            duration: '15s',
        },
    },

    thresholds: {
        checks: ['rate==1'],
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export function setup() {
    const ownerId = `load-test-${Date.now()}`;

    const response = http.post(
        `${BASE_URL}/api/wallets`,
        JSON.stringify({
            ownerId: ownerId,
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

    sleep(1);
}