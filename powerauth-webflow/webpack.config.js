var path = require('path');

var node_dir = __dirname + '/node_modules';

var WebpackDeployPlugin = require('./src/main/js/webpack-deploy.js');
var HardSourceWebpackPlugin = require('hard-source-webpack-plugin');

module.exports = {
    entry: './src/main/js/client.js',
    devtool: 'sourcemaps',
    cache: true,
    resolve: {
        alias: {
            'stompjs': node_dir + '/stompjs/lib/stomp.js',
        }
    },
    output: {
        path: __dirname,
        filename: './src/main/resources/static/resources/js/built/bundle.js'
    },
    module: {
        loaders: [
            {
                test: path.join(__dirname, '.'),
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    cacheDirectory: true,
                    presets: ['es2015', 'react'],
                    plugins: ['transform-object-rest-spread', 'transform-decorators-legacy', 'transform-class-properties']
                }
            }
        ]
    },
    plugins: [
        new WebpackDeployPlugin(),
        new HardSourceWebpackPlugin()
    ]
};