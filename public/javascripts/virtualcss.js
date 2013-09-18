$(function () {
    $('#output').each($(this).children().style)
    html($('#output').html() + 'go<br>' + getSelectorsForProperty('width'))

});

