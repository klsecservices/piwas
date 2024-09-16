// Generated using webpack-cli https://github.com/webpack/webpack-cli

const path = require('path');
const webpack = require('webpack');



const config = {
    entry: './src/index.js',
    externalsPresets: { node: true },
    externalsType: 'commonjs',
    target: 'node21',
    externals: {
        bufferutil: "bufferutil",
        "utf-8-validate": "utf-8-validate",
    },
    output: {
        filename: 'piwas.js',
        path: path.resolve(__dirname, 'dist'),
    },
    plugins: [
    ],
    module: {

    },
};

module.exports = () => {
    return config;
};
