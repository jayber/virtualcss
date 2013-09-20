definitions = {
    layout: function (elem, args) {
        elem.children().css('float', 'left');
        elem.children().last().css('clear', 'right');
    },
    position: function (elem, args) {
        if (args == 'fixed') {
            elem.css('top', '0');
            elem.next().css('padding-top', elem.height() + 'px');
        }
    },
    textVerticalAlign: function (elem, args) {
        if (args == 'middle') {
            elem.wrapInner('<div></div>');
            var container = elem.children().first();
            container.css('display', 'table-cell');
            container.css('verticalAlign', 'middle');
            container.css('height', elem.height() + 'px');
            container.css('width', elem.node.offsetWidth + 'px');
        }
    },
    placement: function (elem, args) {
        if (args == 'center') {
            var unfilled = elem.parent().width() - elem.width();
            var half = unfilled / 2;
            elem.css('margin-left', half + 'px')
        }
    }
}