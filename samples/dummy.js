const a = 10;
const b = 'hello';
const c = null;
const d = a;
const e = 1 + 2 + 3 + 4;
const f = {
    'a': 1,
    'b': 2,
    'c': 3
};
const g = [
    1,
    2,
    3
];
const print_multiple = (x, y, z, a, b, c) => {
    if (1 === 1) {
        console.log('x :', x);
        console.log('y :', y);
        return z;
    } else {
        console.log('a :', a);
        console.log('b :', b);
        return console.log('c :', c);
    }
};
const z = print_multiple(1 + 2 + 3 + 4 + 5 + 6, 'a', true, [
    1,
    2,
    3,
    {
        'a': 1,
        'b': c,
        'c': [
            1,
            2,
            {
                'x': 10,
                'y': [
                    20,
                    30
                ]
            },
            3
        ]
    }
], null, {
    'a': 1,
    'b': [
        1,
        2,
        3,
        {
            'a': 1,
            'b': c,
            'c': [
                1,
                2,
                {
                    'x': 10,
                    'y': [
                        20,
                        30
                    ]
                },
                3
            ]
        }
    ]
});
console.log('z :', z);
