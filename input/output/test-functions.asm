        Label        -mem-manager-initialize   
        DLabel       $heap-start-ptr           
        DataZ        4                         
        DLabel       $heap-after-ptr           
        DataZ        4                         
        DLabel       $heap-first-free          
        DataZ        4                         
        DLabel       $mmgr-newblock-block      
        DataZ        4                         
        DLabel       $mmgr-newblock-size       
        DataZ        4                         
        PushD        $heap-memory              
        Duplicate                              
        PushD        $heap-start-ptr           
        Exchange                               
        StoreI                                 
        PushD        $heap-after-ptr           
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
        DLabel       $stack-pointer            
        DataI        0                         
        DLabel       $frame-pointer            
        DataI        0                         
        PushD        $stack-pointer            
        Memtop                                 
        StoreI                                 
        PushD        $frame-pointer            
        Memtop                                 
        StoreI                                 
        Jump         $$main                    
        DLabel       $eat-location-zero        
        DataZ        8                         
        DLabel       $print-format-integer     
        DataC        37                        %% "%d"
        DataC        100                       
        DataC        0                         
        DLabel       $print-format-float       
        DataC        37                        %% "%f"
        DataC        102                       
        DataC        0                         
        DLabel       $print-format-boolean     
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-character   
        DataC        37                        %% "%c"
        DataC        99                        
        DataC        0                         
        DLabel       $print-format-string      
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-function    
        DataC        60                        %% "<function>"
        DataC        102                       
        DataC        117                       
        DataC        110                       
        DataC        99                        
        DataC        116                       
        DataC        105                       
        DataC        111                       
        DataC        110                       
        DataC        62                        
        DataC        0                         
        DLabel       $print-format-newline     
        DataC        10                        %% "\n"
        DataC        0                         
        DLabel       $print-format-tab         
        DataC        9                         %% "\t"
        DataC        0                         
        DLabel       $print-format-space       
        DataC        32                        %% " "
        DataC        0                         
        DLabel       $boolean-true-string      
        DataC        116                       %% "true"
        DataC        114                       
        DataC        117                       
        DataC        101                       
        DataC        0                         
        DLabel       $boolean-false-string     
        DataC        102                       %% "false"
        DataC        97                        
        DataC        108                       
        DataC        115                       
        DataC        101                       
        DataC        0                         
        DLabel       $errors-general-message   
        DataC        82                        %% "Runtime error: %s\n"
        DataC        117                       
        DataC        110                       
        DataC        116                       
        DataC        105                       
        DataC        109                       
        DataC        101                       
        DataC        32                        
        DataC        101                       
        DataC        114                       
        DataC        114                       
        DataC        111                       
        DataC        114                       
        DataC        58                        
        DataC        32                        
        DataC        37                        
        DataC        115                       
        DataC        10                        
        DataC        0                         
        Label        $$general-runtime-error   
        PushD        $errors-general-message   
        Printf                                 
        Halt                                   
        DLabel       $errors-int-divide-by-zero 
        DataC        105                       %% "integer divide by zero"
        DataC        110                       
        DataC        116                       
        DataC        101                       
        DataC        103                       
        DataC        101                       
        DataC        114                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$i-divide-by-zero        
        PushD        $errors-int-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-float-divide-by-zero 
        DataC        102                       %% "float divide by zero"
        DataC        108                       
        DataC        111                       
        DataC        97                        
        DataC        116                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$f-divide-by-zero        
        PushD        $errors-float-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-negative-index    
        DataC        110                       %% "negative index used for array"
        DataC        101                       
        DataC        103                       
        DataC        97                        
        DataC        116                       
        DataC        105                       
        DataC        118                       
        DataC        101                       
        DataC        32                        
        DataC        105                       
        DataC        110                       
        DataC        100                       
        DataC        101                       
        DataC        120                       
        DataC        32                        
        DataC        117                       
        DataC        115                       
        DataC        101                       
        DataC        100                       
        DataC        32                        
        DataC        102                       
        DataC        111                       
        DataC        114                       
        DataC        32                        
        DataC        97                        
        DataC        114                       
        DataC        114                       
        DataC        97                        
        DataC        121                       
        DataC        0                         
        Label        $$negative-index          
        PushD        $errors-negative-index    
        Jump         $$general-runtime-error   
        DLabel       $errors-out-of-bounds-index 
        DataC        105                       %% "index out of bounds"
        DataC        110                       
        DataC        100                       
        DataC        101                       
        DataC        120                       
        DataC        32                        
        DataC        111                       
        DataC        117                       
        DataC        116                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        98                        
        DataC        111                       
        DataC        117                       
        DataC        110                       
        DataC        100                       
        DataC        115                       
        DataC        0                         
        Label        $$out-of-bounds-index     
        PushD        $errors-out-of-bounds-index 
        Jump         $$general-runtime-error   
        DLabel       $errors-function-runoff   
        DataC        99                        %% "code run off the end of a function"
        DataC        111                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        114                       
        DataC        117                       
        DataC        110                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        102                       
        DataC        32                        
        DataC        116                       
        DataC        104                       
        DataC        101                       
        DataC        32                        
        DataC        101                       
        DataC        110                       
        DataC        100                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        97                        
        DataC        32                        
        DataC        102                       
        DataC        117                       
        DataC        110                       
        DataC        99                        
        DataC        116                       
        DataC        105                       
        DataC        111                       
        DataC        110                       
        DataC        0                         
        Label        $$function-runoff         
        PushD        $errors-function-runoff   
        Jump         $$general-runtime-error   
        DLabel       $array-temp-1             
        DataI        0                         
        DLabel       $array-temp-2             
        DataI        0                         
        DLabel       $array-temp-3             
        DataI        0                         
        DLabel       $array-temp-4             
        DataI        0                         
        DLabel       $array-temp-5             
        DataI        0                         
        DLabel       $array-temp-6             
        DataI        0                         
        DLabel       $print-temp-1             
        DataI        0                         
        DLabel       $print-temp-2             
        DataI        0                         
        DLabel       $print-temp-3             
        DataI        0                         
        DLabel       $index-temp-1             
        DataI        0                         
        DLabel       $index-temp-2             
        DataI        0                         
        DLabel       $func-return-addr-temp    
        DataI        0                         
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        36                        
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% fin
        PushD        -function-1-              
        StoreI                                 
        PushD        $global-memory-block      
        PushI        8                         
        Add                                    %% pax
        PushD        -function-2-              
        StoreI                                 
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% a
        PushI        11                        
        StoreI                                 
        PushD        $global-memory-block      
        PushI        20                        
        Add                                    %% b
        PushI        14                        
        StoreI                                 
        PushD        $global-memory-block      
        PushI        24                        
        Add                                    %% c
        PushI        16                        
        StoreI                                 
        Label        -IF-6-if                  
        Label        -compare-5-arg1           
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% a
        LoadI                                  
        Label        -compare-5-arg2           
        PushI        5                         
        Label        -compare-5-sub            
        Subtract                               
        JumpPos      -compare-5-true           
        Jump         -compare-5-false          
        Label        -compare-5-true           
        PushI        1                         
        Jump         -compare-5-join           
        Label        -compare-5-false          
        PushI        0                         
        Jump         -compare-5-join           
        Label        -compare-5-join           
        JumpFalse    -IF-6-else                
        PushD        $global-memory-block      
        PushI        28                        
        Add                                    %% m
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 1
        LoadI                                  
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% a
        LoadI                                  
        StoreI                                 
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 2
        LoadI                                  
        PushD        $global-memory-block      
        PushI        20                        
        Add                                    %% b
        LoadI                                  
        StoreI                                 
        PushD        $global-memory-block      
        PushI        8                         
        Add                                    %% pax
        LoadI                                  
        CallV                                  
        PushD        $stack-pointer            
        LoadI                                  
        PushD        $stack-pointer            
        LoadI                                  
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            %% restore stack pointer
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    
        StoreI                                 
        StoreI                                 
        PushD        $global-memory-block      
        PushI        28                        
        Add                                    %% m
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        32                        
        Add                                    %% pax
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 1
        LoadI                                  
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% a
        LoadI                                  
        StoreI                                 
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 2
        LoadI                                  
        PushD        $global-memory-block      
        PushI        28                        
        Add                                    %% m
        LoadI                                  
        StoreI                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% fin
        LoadI                                  
        CallV                                  
        PushD        $stack-pointer            
        LoadI                                  
        PushD        $stack-pointer            
        LoadI                                  
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            %% restore stack pointer
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    
        StoreI                                 
        StoreI                                 
        PushD        $global-memory-block      
        PushI        32                        
        Add                                    %% pax
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        Jump         -IF-6-join                
        Label        -IF-6-else                
        Label        -IF-6-join                
        PushD        $global-memory-block      
        PushI        28                        
        Add                                    %% x
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 1
        LoadI                                  
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% a
        LoadI                                  
        StoreI                                 
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 2
        LoadI                                  
        PushD        $global-memory-block      
        PushI        20                        
        Add                                    %% b
        LoadI                                  
        StoreI                                 
        PushD        $global-memory-block      
        PushI        8                         
        Add                                    %% pax
        LoadI                                  
        CallV                                  
        PushD        $stack-pointer            
        LoadI                                  
        PushD        $stack-pointer            
        LoadI                                  
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            %% restore stack pointer
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    
        StoreI                                 
        StoreI                                 
        PushD        $global-memory-block      
        PushI        28                        
        Add                                    %% x
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        Halt                                   %% End of Execution
        Label        -function-1-              
        PushD        $stack-pointer            %% store return addr.
        LoadI                                  
        PushI        -8                        
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            %% store dyn. link
        LoadI                                  
        PushI        -4                        
        Add                                    
        PushD        $frame-pointer            
        LoadI                                  
        StoreI                                 
        PushD        $frame-pointer            %% move frame pointer
        PushD        $stack-pointer            
        LoadI                                  
        StoreI                                 
        PushD        $stack-pointer            %% move stack pointer
        PushD        $stack-pointer            
        LoadI                                  
        PushI        12                        
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 1
        LoadI                                  
        PushI        5                         
        PushD        $frame-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    %% a
        LoadI                                  
        Multiply                               
        StoreI                                 
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 2
        LoadI                                  
        PushD        $frame-pointer            
        LoadI                                  
        PushI        0                         
        Add                                    %% b
        LoadI                                  
        StoreI                                 
        PushD        $global-memory-block      
        PushI        8                         
        Add                                    %% pax
        LoadI                                  
        CallV                                  
        PushD        $stack-pointer            
        LoadI                                  
        PushD        $stack-pointer            
        LoadI                                  
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            %% restore stack pointer
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    
        StoreI                                 
        Jump         -function-1-exit-handshake 
        Label        -function-1-exit-error    
        Jump         $$function-runoff         
        Label        -function-1-exit-handshake 
        PushD        $frame-pointer            %% get return addr.
        LoadI                                  
        PushI        -8                        
        Add                                    
        LoadI                                  
        PushD        $frame-pointer            %% restore frame pointer
        PushD        $frame-pointer            
        LoadI                                  
        PushI        -4                        
        Add                                    
        LoadI                                  
        StoreI                                 
        PushD        $stack-pointer            %% pop frame stack
        PushD        $stack-pointer            
        LoadI                                  
        PushI        12                        
        Add                                    
        PushI        8                         
        Add                                    
        PushI        4                         %% store return val.
        Subtract                               
        StoreI                                 
        PushD        $func-return-addr-temp    
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            
        LoadI                                  
        Exchange                               
        StoreI                                 
        PushD        $func-return-addr-temp    
        LoadI                                  
        Return                                 
        Label        -function-2-              
        PushD        $stack-pointer            %% store return addr.
        LoadI                                  
        PushI        -8                        
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            %% store dyn. link
        LoadI                                  
        PushI        -4                        
        Add                                    
        PushD        $frame-pointer            
        LoadI                                  
        StoreI                                 
        PushD        $frame-pointer            %% move frame pointer
        PushD        $stack-pointer            
        LoadI                                  
        StoreI                                 
        PushD        $stack-pointer            %% move stack pointer
        PushD        $stack-pointer            
        LoadI                                  
        PushI        12                        
        Subtract                               
        StoreI                                 
        Label        -IF-4-if                  
        Label        -compare-3-arg1           
        PushD        $frame-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    %% a
        LoadI                                  
        Label        -compare-3-arg2           
        PushD        $frame-pointer            
        LoadI                                  
        PushI        0                         
        Add                                    %% b
        LoadI                                  
        Label        -compare-3-sub            
        Subtract                               
        JumpNeg      -compare-3-true           
        Jump         -compare-3-false          
        Label        -compare-3-true           
        PushI        1                         
        Jump         -compare-3-join           
        Label        -compare-3-false          
        PushI        0                         
        Jump         -compare-3-join           
        Label        -compare-3-join           
        JumpFalse    -IF-4-else                
        PushD        $frame-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    %% a
        LoadI                                  
        Jump         -function-2-exit-handshake 
        Jump         -IF-4-join                
        Label        -IF-4-else                
        Label        -IF-4-join                
        PushD        $frame-pointer            
        LoadI                                  
        PushI        0                         
        Add                                    %% b
        LoadI                                  
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 1
        LoadI                                  
        PushD        $frame-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    %% a
        LoadI                                  
        StoreI                                 
        PushD        $stack-pointer            
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Subtract                               
        StoreI                                 
        PushD        $stack-pointer            %% store arg 2
        LoadI                                  
        PushI        3                         
        PushD        $frame-pointer            
        LoadI                                  
        PushI        0                         
        Add                                    %% b
        LoadI                                  
        Multiply                               
        StoreI                                 
        PushD        $global-memory-block      
        PushI        8                         
        Add                                    %% pax
        LoadI                                  
        CallV                                  
        PushD        $stack-pointer            
        LoadI                                  
        PushD        $stack-pointer            
        LoadI                                  
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            %% restore stack pointer
        PushD        $stack-pointer            
        LoadI                                  
        PushI        4                         
        Add                                    
        StoreI                                 
        Add                                    
        Jump         -function-2-exit-handshake 
        Label        -function-2-exit-error    
        Jump         $$function-runoff         
        Label        -function-2-exit-handshake 
        PushD        $frame-pointer            %% get return addr.
        LoadI                                  
        PushI        -8                        
        Add                                    
        LoadI                                  
        PushD        $frame-pointer            %% restore frame pointer
        PushD        $frame-pointer            
        LoadI                                  
        PushI        -4                        
        Add                                    
        LoadI                                  
        StoreI                                 
        PushD        $stack-pointer            %% pop frame stack
        PushD        $stack-pointer            
        LoadI                                  
        PushI        12                        
        Add                                    
        PushI        8                         
        Add                                    
        PushI        4                         %% store return val.
        Subtract                               
        StoreI                                 
        PushD        $func-return-addr-temp    
        Exchange                               
        StoreI                                 
        PushD        $stack-pointer            
        LoadI                                  
        Exchange                               
        StoreI                                 
        PushD        $func-return-addr-temp    
        LoadI                                  
        Return                                 
        Label        -mem-manager-make-tags    
        DLabel       $mmgr-tags-size           
        DataZ        4                         
        DLabel       $mmgr-tags-start          
        DataZ        4                         
        DLabel       $mmgr-tags-available      
        DataZ        4                         
        DLabel       $mmgr-tags-nextptr        
        DataZ        4                         
        DLabel       $mmgr-tags-prevptr        
        DataZ        4                         
        DLabel       $mmgr-tags-return         
        DataZ        4                         
        PushD        $mmgr-tags-return         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-size           
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-start          
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-available      
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-nextptr        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-prevptr        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-prevptr        
        LoadI                                  
        PushD        $mmgr-tags-size           
        LoadI                                  
        PushD        $mmgr-tags-available      
        LoadI                                  
        PushD        $mmgr-tags-start          
        LoadI                                  
        Call         -mem-manager-one-tag      
        PushD        $mmgr-tags-nextptr        
        LoadI                                  
        PushD        $mmgr-tags-size           
        LoadI                                  
        PushD        $mmgr-tags-available      
        LoadI                                  
        PushD        $mmgr-tags-start          
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        Call         -mem-manager-one-tag      
        PushD        $mmgr-tags-return         
        LoadI                                  
        Return                                 
        Label        -mem-manager-one-tag      
        DLabel       $mmgr-onetag-return       
        DataZ        4                         
        DLabel       $mmgr-onetag-location     
        DataZ        4                         
        DLabel       $mmgr-onetag-available    
        DataZ        4                         
        DLabel       $mmgr-onetag-size         
        DataZ        4                         
        DLabel       $mmgr-onetag-pointer      
        DataZ        4                         
        PushD        $mmgr-onetag-return       
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-location     
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-available    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-size         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-size         
        LoadI                                  
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        4                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-available    
        LoadI                                  
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushD        $mmgr-onetag-return       
        LoadI                                  
        Return                                 
        Label        -mem-manager-allocate     
        DLabel       $mmgr-alloc-return        
        DataZ        4                         
        DLabel       $mmgr-alloc-size          
        DataZ        4                         
        DLabel       $mmgr-alloc-current-block 
        DataZ        4                         
        DLabel       $mmgr-alloc-remainder-block 
        DataZ        4                         
        DLabel       $mmgr-alloc-remainder-size 
        DataZ        4                         
        PushD        $mmgr-alloc-return        
        Exchange                               
        StoreI                                 
        PushI        18                        
        Add                                    
        PushD        $mmgr-alloc-size          
        Exchange                               
        StoreI                                 
        PushD        $heap-first-free          
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Label        -mmgr-alloc-process-current 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        JumpFalse    -mmgr-alloc-no-block-works 
        Label        -mmgr-alloc-test-block    
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Subtract                               
        PushI        1                         
        Add                                    
        JumpPos      -mmgr-alloc-found-block   
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Jump         -mmgr-alloc-process-current 
        Label        -mmgr-alloc-found-block   
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        Call         -mem-manager-remove-block 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Subtract                               
        PushI        26                        
        Subtract                               
        JumpNeg      -mmgr-alloc-return-userblock 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Add                                    
        PushD        $mmgr-alloc-remainder-block 
        Exchange                               
        StoreI                                 
        PushD        $mmgr-alloc-size          
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        Exchange                               
        Subtract                               
        PushD        $mmgr-alloc-remainder-size 
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushI        0                         
        PushI        0                         
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushI        0                         
        PushI        0                         
        PushI        1                         
        PushD        $mmgr-alloc-remainder-block 
        LoadI                                  
        PushD        $mmgr-alloc-remainder-size 
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushD        $mmgr-alloc-remainder-block 
        LoadI                                  
        PushI        9                         
        Add                                    
        Call         -mem-manager-deallocate   
        Jump         -mmgr-alloc-return-userblock 
        Label        -mmgr-alloc-no-block-works 
        PushD        $mmgr-alloc-size          
        LoadI                                  
        PushD        $mmgr-newblock-size       
        Exchange                               
        StoreI                                 
        PushD        $heap-after-ptr           
        LoadI                                  
        PushD        $mmgr-newblock-block      
        Exchange                               
        StoreI                                 
        PushD        $mmgr-newblock-size       
        LoadI                                  
        PushD        $heap-after-ptr           
        LoadI                                  
        Add                                    
        PushD        $heap-after-ptr           
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushI        0                         
        PushI        0                         
        PushD        $mmgr-newblock-block      
        LoadI                                  
        PushD        $mmgr-newblock-size       
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushD        $mmgr-newblock-block      
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Label        -mmgr-alloc-return-userblock 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        9                         
        Add                                    
        PushD        $mmgr-alloc-return        
        LoadI                                  
        Return                                 
        Label        -mem-manager-deallocate   
        DLabel       $mmgr-dealloc-return      
        DataZ        4                         
        DLabel       $mmgr-dealloc-block       
        DataZ        4                         
        PushD        $mmgr-dealloc-return      
        Exchange                               
        StoreI                                 
        PushI        9                         
        Subtract                               
        PushD        $mmgr-dealloc-block       
        Exchange                               
        StoreI                                 
        PushD        $heap-first-free          
        LoadI                                  
        JumpFalse    -mmgr-bypass-firstFree    
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushD        $heap-first-free          
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        Label        -mmgr-bypass-firstFree    
        PushI        0                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $heap-first-free          
        LoadI                                  
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushI        1                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushI        1                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
        PushD        $mmgr-dealloc-return      
        LoadI                                  
        Return                                 
        Label        -mem-manager-remove-block 
        DLabel       $mmgr-remove-return       
        DataZ        4                         
        DLabel       $mmgr-remove-block        
        DataZ        4                         
        DLabel       $mmgr-remove-prev         
        DataZ        4                         
        DLabel       $mmgr-remove-next         
        DataZ        4                         
        PushD        $mmgr-remove-return       
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        LoadI                                  
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-remove-prev         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-remove-next         
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-process-prev 
        PushD        $mmgr-remove-prev         
        LoadI                                  
        JumpFalse    -mmgr-remove-no-prev      
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushD        $mmgr-remove-prev         
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        Jump         -mmgr-remove-process-next 
        Label        -mmgr-remove-no-prev      
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-process-next 
        PushD        $mmgr-remove-next         
        LoadI                                  
        JumpFalse    -mmgr-remove-done         
        PushD        $mmgr-remove-prev         
        LoadI                                  
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-done         
        PushD        $mmgr-remove-return       
        LoadI                                  
        Return                                 
        DLabel       $heap-memory              
