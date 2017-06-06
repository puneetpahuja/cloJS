let fs = require ("fs");
let gen = require ("escodegen");
fs.writeFileSync(process.argv[3], gen.generate(JSON.parse(fs.readFileSync(process.argv[2], "utf8"))));
