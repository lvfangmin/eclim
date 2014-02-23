let s:command_import =
    \ '-command scala_import -p "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding> -t <missingType>'

function! eclim#scala#import#Import(...)
    if !eclim#project#util#IsCurrentFileInProject(0)
        return
    endif

    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#project#util#GetProjectRelativeFilePath()
    let offset = eclim#util#GetOffset()
    let encoding = eclim#util#GetEncoding()
    let type = expand('<cword>')

    let command = s:command_import
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<offset>', offset, '')
    let command = substitute(command, '<encoding>', encoding, '')
    let command = substitute(command, '<missingType>', type, '')

    if a:0
        let command .= ' -v ' . a:1
    endif

    let result = eclim#Execute(command)

    if type(result) == g:STRING_TYPE
        call eclim#util#EchoError(result)
        return
    endif

    if type(result) == g:DICT_TYPE
        call eclim#util#Reload({'pos': [result.line, result.column]})
        call eclim#lang#UpdateSrcFile('scala')
        if result.offset != offset
            call eclim#util#Echo('Imported ' . type)
        endif
        return
    endif

    if type(result) != g:LIST_TYPE
        return
    endif

    let choice = eclim#scala#import#ImportPrompt(result)
    if choice != ''
        call eclim#scala#import#AddImport(choice)
"        call eclim#scala#import#Import(choice)
    endif
endfunction

function! eclim#scala#import#ImportPrompt(choices) " {{{
  " prompt the user to choose the class to import.
  let response = eclim#util#PromptList("Choose the class to import", a:choices)
  if response == -1
    return ''
  endif

  return get(a:choices, response)
endfunction " }}}

function! eclim#scala#import#AddImport(choice) " {{{
    let save_cursor = getpos(".")
    let content = "import " . a:choice
    let addPos = eclim#scala#import#FindImportPos(content)

    if addPos == -1
        call eclim#util#Echo("Import " . a:choice . " already exist")
        call setpos('.', save_cursor)
        return
    endif

    call append(addPos, content)
    write
    call eclim#util#Reload({'pos': [save_cursor[1] + 1, save_cursor[2]]})
    call eclim#lang#UpdateSrcFile('scala', 1)
    call eclim#util#Echo('Imported ' . a:choice)
    let save_cursor[1] = save_cursor[1] + 1
    call setpos('.', save_cursor)
endfunction
" }}}

function! eclim#scala#import#FindImportPos(content)

    " set the the first col row pos to start search
    call cursor(1, 1)
    let importPos = search("^import", "cW")
    if importPos == 0
        " import not exist
        let packagePos = search("^package", "cW")
        if packagePos == 0
            " package not exist
            return 1
        endif

        return packagePos + 1
    endif

    while importPos != 0
        let importContent = getline(importPos)
        if a:content < importContent
            return importPos - 1
        elseif a:content == importContent
            return -1
        else
            " set to next line
            let preImportPos = importPos
            call cursor(importPos + 1, 1)
            let importPos = search("^import", "cW")
            if importPos == 0
                return preImportPos + 1
            endif
        endif
    endwhile
endfunction
