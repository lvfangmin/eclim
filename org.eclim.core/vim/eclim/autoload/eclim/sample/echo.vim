let s:echo_command = 
    \ '-command echo -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding>'

function! eclim#sample#echo#Echo()
    if !eclim#project#util#IsCurrentFileInProject(0)
        return
    endif

    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#project#util#GetProjectRelativeFilePath()
    
    let command = s:echo_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<offset>', eclim#util#GetOffset(), '')
    let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

    let response = eclim#Execute(command)

    if type(response) != g:DICT_TYPE
        return
    endif

    call eclim#util#Echo(string(response))
endfunction
