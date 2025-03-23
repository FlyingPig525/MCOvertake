// @ts-ignore
import fs from 'fs'

// i forgot ts :skull:

type map = {
    [key: string]: any
}

let file: map = JSON.parse(fs.readFileSync("block-data.json5", {
    encoding: "utf8"
}))
const copy = structuredClone(file)
// @ts-ignore
for (const [key, value] of Object.entries(copy)) {
    console.log(value)
    value["power"] = [value["power"], 2.0]
    value["organicMatter"] = [value["organicMatter"], 2.0]
    file[key] = value
}
fs.writeFileSync("block-data.json5", JSON.stringify(file))