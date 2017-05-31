$(document).ready(function() {
    $('#edit-section').hide();

    $('#edit-post').click(function() {
        $('#content-section').hide();
        nicEditors.editors[0].nicInstances[0].elm.innerHTML = nicEditors.editors[0].nicInstances[0].copyElm.defaultValue;
        $('#edit-section').show();
    });
});