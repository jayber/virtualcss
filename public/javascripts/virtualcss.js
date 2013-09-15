$(function () {
    $('#output').html($('#output').html() + 'go<br>' + getSelectorsForProperty('width'))

});

function getSelectorsForProperty(property) {
    var selectors = []
    var sheets;
    alert('stylesheets: ' + document.styleSheets.length)
    for (sheets = document.styleSheets.length - 1; sheets >= 0; sheets--) {


//        alert('stylesheet: '+ document.styleSheets[sheets])
        var rules = document.styleSheets[sheets].rules || document.styleSheets[sheets].cssRules
        for (var x = 0; x < rules.length; x++) {
            var rule = rules[x]
//            alert('rule: '+ rule.style.cssText)
            for (var z = 0; z < rule.style.length; z++) {
                var item = rule.style.item(z)
                alert('item: ' + item)

                if (z == property) {
                    selectors.push(rule.selectorText)
                }
            }
        }
    }
    return selectors
}